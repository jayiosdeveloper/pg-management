package com.pg.management.ui.screens.admin.workers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.model.WorkerCredentialsInfo
import com.pg.management.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerCredentialsUi(
    val loading: Boolean = true,
    val info: WorkerCredentialsInfo? = null,
    val newPasswordInput: String = "",
    val newPassword: String? = null,
    val resetting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class WorkerCredentialsViewModel @Inject constructor(
    private val workers: WorkerRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val workerId: String = checkNotNull(savedState["workerId"]) { "workerId arg missing" }
    private val _s = MutableStateFlow(WorkerCredentialsUi())
    val state: StateFlow<WorkerCredentialsUi> = _s.asStateFlow()

    init { load() }

    fun onNewPasswordInputChange(v: String) = _s.update { it.copy(newPasswordInput = v, error = null) }

    private fun load() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try { _s.update { it.copy(loading = false, info = workers.credentials(workerId)) } }
            catch (e: Throwable) { _s.update { it.copy(loading = false, error = e.message ?: "Failed") } }
        }
    }

    fun reset(useAutoGen: Boolean) {
        val custom = if (useAutoGen) null else _s.value.newPasswordInput.trim().ifBlank { null }
        if (!useAutoGen && (custom == null || custom.length < 6)) {
            _s.update { it.copy(error = "Password must be at least 6 characters") }; return
        }
        _s.update { it.copy(resetting = true, error = null) }
        viewModelScope.launch {
            try {
                val creds = workers.resetPassword(workerId, custom)
                _s.update { it.copy(resetting = false, newPassword = creds.tempPassword, newPasswordInput = "") }
            } catch (e: Throwable) {
                _s.update { it.copy(resetting = false, error = e.message ?: "Reset failed") }
            }
        }
    }
}
