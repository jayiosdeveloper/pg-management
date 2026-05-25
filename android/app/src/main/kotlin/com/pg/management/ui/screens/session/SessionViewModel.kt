package com.pg.management.ui.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.auth.AuthSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val session: AuthSession? = null,
    val loggingOut: Boolean = false,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val local = MutableStateFlow(SessionUiState())

    val state: StateFlow<SessionUiState> = combine(local, repo.sessionFlow) { s, session ->
        s.copy(session = session)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionUiState())

    fun logout() {
        if (local.value.loggingOut) return
        local.update { it.copy(loggingOut = true) }
        viewModelScope.launch {
            runCatching { repo.logout() }
            local.update { it.copy(loggingOut = false, loggedOut = true) }
        }
    }

    fun consumeLogout() = local.update { it.copy(loggedOut = false) }
}
