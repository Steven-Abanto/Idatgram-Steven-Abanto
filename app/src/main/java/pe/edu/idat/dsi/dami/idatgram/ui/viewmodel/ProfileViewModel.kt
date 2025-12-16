package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.User
import pe.edu.idat.dsi.dami.idatgram.data.repository.PostRepository
import pe.edu.idat.dsi.dami.idatgram.data.repository.UserRepository
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val userPosts: List<PostWithUser> = emptyList(),
    val isOwnProfile: Boolean = false,
    val isFollowing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentTargetId: String? = null

    fun load(targetUserId: String) {
        if (currentTargetId == targetUserId) return
        currentTargetId = targetUserId

        _uiState.update { it.copy(isLoading = true, error = null) }

        // 1) Carga info de usuario + relación (isFollowing / ownProfile)
        viewModelScope.launch {
            runCatching {
                // Asegúrate de que el repo tenga sesión cargada
                // (si ya lo haces en SessionViewModel, puedes quitar esto)
                userRepository.loadSession()

                val currentId = userRepository.getCurrentUserId()
                val user = userRepository.getUserById(targetUserId)

                val isOwn = (currentId != null && currentId == targetUserId)

                // Para own profile puedes ignorar isFollowing
                val profile = if (!isOwn) userRepository.getUserProfile(targetUserId) else null

                Triple(user, isOwn, profile?.isFollowing ?: false)
            }.onSuccess { (user, isOwn, isFollowing) ->
                if (user == null) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Usuario no encontrado")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isOwnProfile = isOwn,
                            isFollowing = isFollowing
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Error cargando perfil")
                }
            }
        }

        // 2) Observa posts con detalles (Flow Room)
        viewModelScope.launch {
            postRepository.getUserPostsWithDetails(targetUserId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error cargando posts") }
                }
                .collect { posts ->
                    _uiState.update { it.copy(userPosts = posts) }
                }
        }
    }

    fun onFollowClick(targetUserId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isOwnProfile) return@launch

            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (state.isFollowing) {
                userRepository.unfollowUser(targetUserId)
            } else {
                userRepository.followUser(targetUserId)
            }

            result.onSuccess { nowFollowing ->
                // Recalcula contadores (followers/following/postsCount)
                // para que tu UI muestre datos correctos
                userRepository.updateUserStatistics(targetUserId)
                userRepository.getCurrentUserId()?.let { me ->
                    userRepository.updateUserStatistics(me)
                }

                // Recarga el usuario local para reflejar counts actualizados
                val updated = userRepository.getUserById(targetUserId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isFollowing = nowFollowing,
                        user = updated ?: it.user
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Error en follow")
                }
            }
        }
    }

    fun refresh(targetUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                userRepository.updateUserStatistics(targetUserId)
                userRepository.getCurrentUserId()?.let { me ->
                    userRepository.updateUserStatistics(me)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error refrescando") }
            }

            val updated = userRepository.getUserById(targetUserId)
            _uiState.update { it.copy(isLoading = false, user = updated ?: it.user) }
        }
    }
}
