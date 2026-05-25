package com.pg.management.ui.screens.admin.tenants

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.model.Tenant
import com.pg.management.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UploadKind { PHOTO, AADHAAR_FRONT, AADHAAR_BACK }

data class TenantDetailUi(
    val loading: Boolean = false,
    val uploading: UploadKind? = null,
    val deleting: Boolean = false,
    val tenant: Tenant? = null,
    val error: String? = null,
    val deleted: Boolean = false,
)

@HiltViewModel
class TenantDetailViewModel @Inject constructor(
    private val repo: TenantRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val tenantId: String = checkNotNull(savedState["tenantId"]) { "tenantId arg missing" }
    private val _state = MutableStateFlow(TenantDetailUi())
    val state: StateFlow<TenantDetailUi> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val t = repo.get(tenantId)
                _state.update { it.copy(loading = false, tenant = t) }
            } catch (e: Throwable) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun upload(kind: UploadKind, uri: Uri) {
        _state.update { it.copy(uploading = kind, error = null) }
        viewModelScope.launch {
            try {
                when (kind) {
                    UploadKind.PHOTO -> repo.uploadPhoto(tenantId, uri)
                    UploadKind.AADHAAR_FRONT -> repo.uploadAadhaarFront(tenantId, uri)
                    UploadKind.AADHAAR_BACK -> repo.uploadAadhaarBack(tenantId, uri)
                }
                refresh()
                _state.update { it.copy(uploading = null) }
            } catch (e: Throwable) {
                _state.update { it.copy(uploading = null, error = e.message ?: "Upload failed") }
            }
        }
    }

    fun delete() {
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            try {
                repo.delete(tenantId)
                _state.update { it.copy(deleting = false, deleted = true) }
            } catch (e: Throwable) {
                _state.update { it.copy(deleting = false, error = e.message ?: "Delete failed") }
            }
        }
    }
}
