package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.repository.PostRepository
import pe.edu.idat.dsi.dami.idatgram.data.repository.UserRepository
import javax.inject.Inject

data class AddPostUiState(
    val imageUrl: String = "",
    val caption: String = "",
    val location: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPostUiState())
    val uiState: StateFlow<AddPostUiState> = _uiState.asStateFlow()

    fun updateImageUrl(url: String) {
        _uiState.value = _uiState.value.copy(imageUrl = url)
    }

    fun updateCaption(caption: String) {
        _uiState.value = _uiState.value.copy(caption = caption)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun createPost() {
        val currentState = _uiState.value
        
        if (currentState.imageUrl.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Debes seleccionar una imagen"
            )
            return
        }

        if (currentState.caption.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Debes agregar una descripción"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val result = postRepository.createPost(
                    caption = currentState.caption,
                    imageUrl = currentState.imageUrl,
                    location = currentState.location
                )

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al crear el post"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = AddPostUiState()
    }

    // Por ahora simulare la seleccion de una imagen con URL de ejemplo
    // Esto se puede reemplazar con un selector de imágenes real
    // Lo veremos mas adelante en el curso
    fun selectSampleImage() {
        val sampleImages = listOf(
            "https://picsum.photos/800/800?random=1",
            "https://picsum.photos/800/800?random=2",
            "https://picsum.photos/800/800?random=3",
            "https://picsum.photos/800/800?random=4",
            "https://picsum.photos/800/800?random=5"
        )
        
        val randomImage = sampleImages.random()
        updateImageUrl(randomImage)
    }
}