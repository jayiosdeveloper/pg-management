package com.pg.management.ui.screens.admin.credentials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.repository.MemberCredentialsInfo
import com.pg.management.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CredentialsUi(
    val loading: Boolean = true,
    val info: MemberCredentialsInfo? = null,
    val newPasswordInput: String = "",
    val newPassword: String? = null,             // shown to admin once reset succeeds
    val resetting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MemberCredentialsViewModel @Inject constructor(
    private val tenants: TenantRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val tenantId: String = checkNotNull(savedState["tenantId"]) { "tenantId arg missing" }
    private val _s = MutableStateFlow(CredentialsUi())
    val state: StateFlow<CredentialsUi> = _s.asStateFlow()

    init { load() }

    fun onNewPasswordInputChange(v: String) = _s.update { it.copy(newPasswordInput = v, error = null) }

    private fun load() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                _s.update { it.copy(loading = false, info = tenants.credentials(tenantId)) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    /** Reset password. If [useAutoGen] is true, server auto-generates. */
    fun reset(useAutoGen: Boolean) {
        val custom = if (useAutoGen) null else _s.value.newPasswordInput.trim().ifBlank { null }
        if (!useAutoGen && (custom == null || custom.length < 6)) {
            _s.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        _s.update { it.copy(resetting = true, error = null) }
        viewModelScope.launch {
            try {
                val creds = tenants.resetPassword(tenantId, custom)
                _s.update { it.copy(resetting = false, newPassword = creds.tempPassword, newPasswordInput = "") }
            } catch (e: Throwable) {
                _s.update { it.copy(resetting = false, error = e.message ?: "Reset failed") }
            }
        }
    }
}
