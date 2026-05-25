package com.pg.management.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.network.ApiException
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val remember: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val success: UserRole? = null,
) {
    val canSubmit: Boolean
        get() = identifier.isNotBlank() && password.length >= 6 && !loading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onIdentifierChange(v: String) = _state.update { it.copy(identifier = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onRememberToggle(v: Boolean) = _state.update { it.copy(remember = v) }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val session = repo.login(s.identifier.trim(), s.password, s.remember)
                _state.update { it.copy(loading = false, success = session.role) }
            } catch (e: ApiException) {
                _state.update { it.copy(loading = false, error = e.message ?: "Login failed") }
            } catch (e: Throwable) {
                _state.update { it.copy(loading = false, error = e.message ?: "Unexpected error") }
            }
        }
    }

    fun consumeSuccess() = _state.update { it.copy(success = null) }
}
