package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryWithUser
import pe.edu.idat.dsi.dami.idatgram.data.repository.PostRepository
import pe.edu.idat.dsi.dami.idatgram.data.repository.StoryRepository
import pe.edu.idat.dsi.dami.idatgram.data.repository.UserRepository
import javax.inject.Inject

data class HomeUiState(
    val posts: List<PostWithUser> = emptyList(),
    val stories: List<StoryWithUser> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
//         viewModelScope.launch {
//             userRepository.loadSession()
//             loadPosts()
//         }

        //Cargamos sesión y sincronizamos API → Room y luego cargamos feed
        viewModelScope.launch {
            userRepository.loadSession()
            userRepository.currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    observeStories()
                    refreshFromRemoteThenLoad()
                } else {
                    _uiState.value = _uiState.value.copy(posts = emptyList(), isLoading = false)
                }
            }
        }
    }

    private var storiesJob: Job? = null

    private fun observeStories() {
        // Evita duplicar collectors si el flow de sesión emite varias veces
        storiesJob?.cancel()
        storiesJob = viewModelScope.launch {
            storyRepository.getFeedStories().collect { stories ->
                _uiState.value = _uiState.value.copy(stories = stories)
            }
        }
    }

    private fun refreshFromRemoteThenLoad() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val postsRemoteResult = postRepository.refreshFeed()
                if (postsRemoteResult.isFailure) {
                    // Si falla remoto, no se bloequa: muestra mensaje y sigue con local
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se pudo sincronizar con el servidor: ${postsRemoteResult.exceptionOrNull()?.message}"
                    )
                }

                // Sync stories
                val storiesRemoteResult = storyRepository.refreshStories()
                if (storiesRemoteResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se pudo sincronizar stories: ${storiesRemoteResult.exceptionOrNull()?.message}"
                    )
                }

                // Cargamos Room
                val posts = postRepository.getFeedPosts()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar posts: ${e.message}"
                )
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val posts = postRepository.getFeedPosts()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar posts: ${e.message}"
                )
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            try {
//                Simular delay de red
//                kotlinx.coroutines.delay(1000)
//                loadPosts()
//                _uiState.value = _uiState.value.copy(isRefreshing = false)

                // Refresh desde servidor
                val remoteResult = postRepository.refreshFeed()
                if (remoteResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al actualizar desde servidor: ${remoteResult.exceptionOrNull()?.message}"
                    )
                }

                // Carga local
                val posts = postRepository.getFeedPosts()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = "Error al actualizar: ${e.message}"
                )
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val result = postRepository.toggleLike(postId)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al cambiar like: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    // Refresh posts to update like status
                    loadPosts()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cambiar like: ${e.message}"
                )
            }
        }
    }

    fun toggleSave(postId: String) {
        viewModelScope.launch {
            try {
                val result = postRepository.toggleSavePost(postId)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al guardar: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    loadPosts()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al guardar: ${e.message}"
                )
            }
        }
    }


    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
