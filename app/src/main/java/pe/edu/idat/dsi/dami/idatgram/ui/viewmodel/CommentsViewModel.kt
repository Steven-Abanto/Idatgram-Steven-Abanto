package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.entity.CommentWithUser
import pe.edu.idat.dsi.dami.idatgram.data.repository.CommentRepository
import javax.inject.Inject

data class CommentsUiState(
    val comments: List<CommentWithUser> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val postId: String = savedStateHandle["postId"] ?: ""

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    init {
        if (postId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Post inválido"
            )
        }

        // Escucha comentarios del post
        viewModelScope.launch {
            commentRepository.getPostCommentsWithUser(postId)
                .collect { list ->
                    _uiState.value = _uiState.value.copy(comments = list)
                }
        }
    }

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(input = value, errorMessage = null)
    }

    fun sendComment() {
        val text = _uiState.value.input
        if (text.trim().isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null)

            val result = commentRepository.addComment(postId = postId, text = text)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al comentar"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    input = ""
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
