package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryWithUser
import pe.edu.idat.dsi.dami.idatgram.data.repository.StoryRepository
import javax.inject.Inject

data class StoryViewerUiState(
    val isLoading: Boolean = false,
    val userId: String = "",
    val stories: List<StoryWithUser> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null
)

@HiltViewModel
class StoryViewerViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryViewerUiState())
    val uiState: StateFlow<StoryViewerUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userId = userId, error = null, currentIndex = 0) }

            storyRepository.getUserStories(userId)
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, stories = list) }
                    // Marca visto el primero si existe
                    list.firstOrNull()?.let { markViewed(it.story.id) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") }
                }
        }
    }

    fun next(onFinished: () -> Unit) {
        val st = _uiState.value
        val nextIndex = st.currentIndex + 1
        if (nextIndex >= st.stories.size) {
            onFinished()
            return
        }
        _uiState.update { it.copy(currentIndex = nextIndex) }
        markViewed(st.stories[nextIndex].story.id)
    }

    fun prev() {
        val st = _uiState.value
        val prevIndex = (st.currentIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentIndex = prevIndex) }
    }

    private fun markViewed(storyId: String) {
        viewModelScope.launch { storyRepository.markViewed(storyId) }
    }
}
