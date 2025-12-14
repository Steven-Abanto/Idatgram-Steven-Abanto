package pe.edu.idat.dsi.dami.idatgram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // true si hay userId guardado en DataStore
    val isLoggedIn: StateFlow<Boolean> =
        userRepository.currentUserIdFlow
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        // Cargamos sesión en memoria para que los repos que usan currentUserId no fallen
        viewModelScope.launch {
            userRepository.loadSession()
        }
    }
}
