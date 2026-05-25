package com.pg.management.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.model.UserRole
import com.pg.management.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val ready: Boolean = false,
    val startDestination: String = Routes.LOGIN,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Keep splash visible briefly for polish
            delay(450)
            val session = repo.currentSession()
            val dest = when (session?.role) {
                UserRole.Admin -> Routes.ADMIN_HOME
                UserRole.Tenant -> Routes.TENANT_HOME
                null -> Routes.LOGIN
            }
            _state.value = SplashUiState(ready = true, startDestination = dest)
        }
    }
}
