package com.pg.management.ui.screens.admin.tenants

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.model.Room
import com.pg.management.domain.model.TenantCredentials
import com.pg.management.domain.repository.RoomRepository
import com.pg.management.domain.repository.TenantInput
import com.pg.management.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TenantFormUi(
    val tenantId: String? = null,                // null = create
    val loading: Boolean = false,
    val saving: Boolean = false,
    val rooms: List<Room> = emptyList(),
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",                     // "" | "Male" | "Female" | "Other"
    val city: String = "",
    val state: String = "",
    val address: String = "",
    val emergencyName: String = "",
    val emergencyPhone: String = "",
    val occupation: String = "",
    val idProofType: String = "aadhaar",
    val idProofNumber: String = "",
    val aadhaarWasSet: Boolean = false,          // existing Aadhaar number, read-only
    val photoUrl: String? = null,
    val aadhaarFrontUrl: String? = null,
    val aadhaarBackUrl: String? = null,
    val uploadingPhoto: Boolean = false,
    val uploadingAadhaarFront: Boolean = false,
    val uploadingAadhaarBack: Boolean = false,
    val roomId: String? = null,
    val bedId: String? = null,
    val joiningDate: String = LocalDate.now().toString(),
    val monthlyRent: String = "",
    val securityDeposit: String = "",
    val notes: String = "",
    val error: String? = null,
    val savedCredentials: TenantCredentials? = null,
    val savedTenantId: String? = null,
) {
    val canSubmit: Boolean get() =
        fullName.isNotBlank() &&
        joiningDate.isNotBlank() &&
        idProofNumber.isNotBlank() &&            // Aadhaar number is compulsory
        !saving
}

@HiltViewModel
class TenantFormViewModel @Inject constructor(
    private val tenants: TenantRepository,
    private val rooms: RoomRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val _state: MutableStateFlow<TenantFormUi>
    val state: StateFlow<TenantFormUi>

    init {
        // Treat empty string as null so the "Add Member" flow is not mistaken for "Edit".
        val rawId: String? = savedState["tenantId"]
        val initialTenantId = rawId?.takeIf { it.isNotBlank() }
        _state = MutableStateFlow(TenantFormUi(tenantId = initialTenantId))
        state = _state.asStateFlow()
        loadRooms()
        initialTenantId?.let { load(it) }
    }

    fun update(transform: (TenantFormUi) -> TenantFormUi) = _state.update(transform)

    private fun loadRooms() {
        viewModelScope.launch {
            try { _state.update { it.copy(rooms = rooms.list(status = "all")) } }
            catch (_: Throwable) { /* tolerate */ }
        }
    }

    private fun load(id: String) {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                val t = tenants.get(id)
                _state.update {
                    it.copy(
                        loading = false,
                        fullName = t.user.fullName,
                        email = t.user.email.orEmpty(),
                        phone = t.user.phone.orEmpty(),
                        gender = t.gender.orEmpty(),
                        city = t.city.orEmpty(),
                        state = t.state.orEmpty(),
                        address = t.address.orEmpty(),
                        emergencyName = t.emergencyContactName.orEmpty(),
                        emergencyPhone = t.emergencyContactPhone.orEmpty(),
                        occupation = t.occupation.orEmpty(),
                        idProofType = t.idProofType ?: "aadhaar",
                        idProofNumber = t.idProofNumber.orEmpty(),
                        aadhaarWasSet = !t.idProofNumber.isNullOrBlank(),
                        photoUrl = t.photoUrl,
                        aadhaarFrontUrl = t.aadhaarFrontUrl,
                        aadhaarBackUrl = t.aadhaarBackUrl,
                        roomId = t.room?.id,
                        bedId = t.bed?.id,
                        joiningDate = t.joiningDate,
                        monthlyRent = t.monthlyRent?.toString().orEmpty(),
                        securityDeposit = t.securityDeposit?.toString().orEmpty(),
                        notes = t.notes.orEmpty(),
                    )
                }
            } catch (e: Throwable) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val input = TenantInput(
                    fullName = s.fullName.trim(),
                    email = s.email.trim().ifBlank { null },
                    phone = s.phone.trim().ifBlank { null },
                    gender = s.gender.trim().ifBlank { null },
                    city = s.city.trim().ifBlank { null },
                    state = s.state.trim().ifBlank { null },
                    address = s.address.trim().ifBlank { null },
                    emergencyContactName = s.emergencyName.trim().ifBlank { null },
                    emergencyContactPhone = s.emergencyPhone.trim().ifBlank { null },
                    occupation = s.occupation.trim().ifBlank { null },
                    idProofType = s.idProofType.trim().ifBlank { null },
                    // If aadhaar was already set, don't send number (we keep it server-side)
                    idProofNumber = if (s.aadhaarWasSet) null else s.idProofNumber.trim().ifBlank { null },
                    roomId = s.roomId,
                    bedId = s.bedId,
                    joiningDate = s.joiningDate,
                    monthlyRent = s.monthlyRent.toDoubleOrNull(),
                    securityDeposit = s.securityDeposit.toDoubleOrNull(),
                    notes = s.notes.trim().ifBlank { null },
                )
                if (s.tenantId == null) {
                    val (tenant, creds) = tenants.create(input)
                    _state.update { it.copy(saving = false, tenantId = tenant.id, aadhaarWasSet = true, savedCredentials = creds, savedTenantId = tenant.id) }
                } else {
                    val updated = tenants.update(s.tenantId, input)
                    _state.update { it.copy(saving = false, aadhaarWasSet = !updated.idProofNumber.isNullOrBlank(), savedTenantId = updated.id) }
                }
            } catch (e: Throwable) {
                _state.update { it.copy(saving = false, error = e.message ?: "Save failed") }
            }
        }
    }

    fun uploadPhoto(uri: Uri) = upload(uri,
        onStart = { _state.update { it.copy(uploadingPhoto = true, error = null) } },
        onSuccess = { url -> _state.update { it.copy(uploadingPhoto = false, photoUrl = url) } },
        onFail = { err -> _state.update { it.copy(uploadingPhoto = false, error = err) } },
        call = { id, u -> tenants.uploadPhoto(id, u) },
    )

    fun uploadAadhaarFront(uri: Uri) = upload(uri,
        onStart = { _state.update { it.copy(uploadingAadhaarFront = true, error = null) } },
        onSuccess = { url -> _state.update { it.copy(uploadingAadhaarFront = false, aadhaarFrontUrl = url) } },
        onFail = { err -> _state.update { it.copy(uploadingAadhaarFront = false, error = err) } },
        call = { id, u -> tenants.uploadAadhaarFront(id, u) },
    )

    fun uploadAadhaarBack(uri: Uri) = upload(uri,
        onStart = { _state.update { it.copy(uploadingAadhaarBack = true, error = null) } },
        onSuccess = { url -> _state.update { it.copy(uploadingAadhaarBack = false, aadhaarBackUrl = url) } },
        onFail = { err -> _state.update { it.copy(uploadingAadhaarBack = false, error = err) } },
        call = { id, u -> tenants.uploadAadhaarBack(id, u) },
    )

    private fun upload(
        uri: Uri,
        onStart: () -> Unit,
        onSuccess: (String) -> Unit,
        onFail: (String) -> Unit,
        call: suspend (String, Uri) -> String,
    ) {
        val tid = _state.value.tenantId
        if (tid == null) {
            onFail("Save the member first to upload photos")
            return
        }
        onStart()
        viewModelScope.launch {
            try { onSuccess(call(tid, uri)) }
            catch (e: Throwable) { onFail(e.message ?: "Upload failed") }
        }
    }

    fun acknowledgeSave() = _state.update { it.copy(savedCredentials = null, savedTenantId = null) }
}
