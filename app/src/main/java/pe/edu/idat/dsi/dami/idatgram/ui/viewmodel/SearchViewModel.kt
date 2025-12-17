package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.User
import pe.edu.idat.dsi.dami.idatgram.data.repository.PostRepository
import pe.edu.idat.dsi.dami.idatgram.data.repository.UserRepository
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val posts: List<PostWithUser> = emptyList(),
    val explorePosts: List<PostWithUser> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // carga explore al entrar
        loadExplore()
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, error = null) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce simple

            val q = newQuery.trim()
            if (q.isBlank()) {
                // Si no hay query, muestra Explore y limpia resultados
                _uiState.update { it.copy(isLoading = false, users = emptyList(), posts = emptyList()) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                // En paralelo (simple)
                val users = userRepository.searchUsers(q, limit = 20)
                val posts = postRepository.searchPosts(q, limit = 50)
                users to posts
            }.onSuccess { (users, posts) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = users,
                        posts = posts
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error buscando") }
            }
        }
    }

    fun loadExplore(limit: Int = 30, offset: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                postRepository.getExplorePosts(limit = limit, offset = offset)
            }.onSuccess { posts ->
                _uiState.update { it.copy(isLoading = false, explorePosts = posts) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error cargando explore") }
            }
        }
    }

    fun refreshExplore() = loadExplore(limit = 30, offset = 0)
}
