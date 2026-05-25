package com.pg.management.ui.screens.admin.workers

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.SalaryPayment
import com.pg.management.domain.model.WorkerCredentials
import com.pg.management.domain.repository.WorkerInput
import com.pg.management.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WorkerFormUi(
    val workerId: String? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val deleting: Boolean = false,
    val deleted: Boolean = false,

    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val roleTitle: String = "",
    val monthlySalary: String = "",
    val joiningDate: String = LocalDate.now().toString(),
    val leavingDate: String = "",
    val gender: String = "",
    val city: String = "",
    val state: String = "",
    val address: String = "",
    val emergencyName: String = "",
    val emergencyPhone: String = "",
    val idProofType: String = "aadhaar",
    val idProofNumber: String = "",
    val aadhaarWasSet: Boolean = false,
    val notes: String = "",
    val status: String = "active",

    val photoUrl: String? = null,
    val aadhaarFrontUrl: String? = null,
    val aadhaarBackUrl: String? = null,
    val uploadingPhoto: Boolean = false,
    val uploadingAadhaarFront: Boolean = false,
    val uploadingAadhaarBack: Boolean = false,

    val salaryPayments: List<SalaryPayment> = emptyList(),
    val recordingSalary: Boolean = false,
    val showSalaryDialog: Boolean = false,

    val savedCredentials: WorkerCredentials? = null,
    val savedWorkerId: String? = null,

    val error: String? = null,
    val message: String? = null,
) {
    val canSubmit: Boolean get() =
        fullName.isNotBlank() && joiningDate.isNotBlank() && idProofNumber.isNotBlank() && !saving
}

@HiltViewModel
class WorkerFormViewModel @Inject constructor(
    private val workers: WorkerRepository,
    private val refreshEvents: RefreshEvents,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val _s: MutableStateFlow<WorkerFormUi>
    val state: StateFlow<WorkerFormUi>

    init {
        val raw: String? = savedState["workerId"]
        val initial = raw?.takeIf { it.isNotBlank() }
        _s = MutableStateFlow(WorkerFormUi(workerId = initial))
        state = _s.asStateFlow()
        initial?.let { load(it) }
    }

    fun update(transform: (WorkerFormUi) -> WorkerFormUi) = _s.update(transform)
    fun acknowledgeSave() = _s.update { it.copy(savedCredentials = null, savedWorkerId = null) }
    fun consumeMessage() = _s.update { it.copy(message = null) }
    fun openSalary(open: Boolean) = _s.update { it.copy(showSalaryDialog = open) }

    private fun load(id: String) {
        _s.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                val w = workers.get(id)
                val sal = runCatching { workers.listSalary(workerId = id) }.getOrDefault(emptyList())
                _s.update {
                    it.copy(
                        loading = false,
                        fullName = w.user.fullName,
                        email = w.user.email.orEmpty(),
                        phone = w.user.phone.orEmpty(),
                        roleTitle = w.roleTitle.orEmpty(),
                        monthlySalary = w.monthlySalary.takeIf { v -> v > 0 }?.toString().orEmpty(),
                        joiningDate = w.joiningDate,
                        leavingDate = w.leavingDate.orEmpty(),
                        gender = w.gender.orEmpty(),
                        city = w.city.orEmpty(),
                        state = w.state.orEmpty(),
                        address = w.address.orEmpty(),
                        emergencyName = w.emergencyContactName.orEmpty(),
                        emergencyPhone = w.emergencyContactPhone.orEmpty(),
                        idProofType = w.idProofType ?: "aadhaar",
                        idProofNumber = w.idProofNumber.orEmpty(),
                        aadhaarWasSet = !w.idProofNumber.isNullOrBlank(),
                        notes = w.notes.orEmpty(),
                        status = w.status,
                        photoUrl = w.photoUrl,
                        aadhaarFrontUrl = w.aadhaarFrontUrl,
                        aadhaarBackUrl = w.aadhaarBackUrl,
                        salaryPayments = sal,
                    )
                }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun submit() {
        val s = _s.value
        if (!s.canSubmit) return
        _s.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val input = WorkerInput(
                    fullName = s.fullName.trim(),
                    email = s.email.trim().ifBlank { null },
                    phone = s.phone.trim().ifBlank { null },
                    roleTitle = s.roleTitle.trim().ifBlank { null },
                    monthlySalary = s.monthlySalary.toDoubleOrNull() ?: 0.0,
                    joiningDate = s.joiningDate,
                    leavingDate = s.leavingDate.trim().ifBlank { null },
                    gender = s.gender.trim().ifBlank { null },
                    city = s.city.trim().ifBlank { null },
                    state = s.state.trim().ifBlank { null },
                    address = s.address.trim().ifBlank { null },
                    emergencyContactName = s.emergencyName.trim().ifBlank { null },
                    emergencyContactPhone = s.emergencyPhone.trim().ifBlank { null },
                    idProofType = s.idProofType.trim().ifBlank { null },
                    idProofNumber = if (s.aadhaarWasSet) null else s.idProofNumber.trim().ifBlank { null },
                    notes = s.notes.trim().ifBlank { null },
                    status = if (s.workerId != null) s.status else null,
                )
                if (s.workerId == null) {
                    val (w, creds) = workers.create(input)
                    refreshEvents.notifyWorkersChanged()
                    _s.update { it.copy(saving = false, workerId = w.id, aadhaarWasSet = true, savedCredentials = creds, savedWorkerId = w.id) }
                } else {
                    val w = workers.update(s.workerId, input)
                    refreshEvents.notifyWorkersChanged()
                    _s.update { it.copy(saving = false, aadhaarWasSet = !w.idProofNumber.isNullOrBlank(), savedWorkerId = w.id) }
                }
            } catch (e: Throwable) {
                _s.update { it.copy(saving = false, error = e.message ?: "Save failed") }
            }
        }
    }

    fun delete() {
        val id = _s.value.workerId ?: return
        _s.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            try {
                workers.delete(id)
                refreshEvents.notifyWorkersChanged()
                _s.update { it.copy(deleting = false, deleted = true) }
            } catch (e: Throwable) {
                _s.update { it.copy(deleting = false, error = e.message ?: "Delete failed") }
            }
        }
    }

    fun uploadPhoto(uri: Uri) = upload(uri,
        onStart = { _s.update { it.copy(uploadingPhoto = true, error = null) } },
        onDone = { url -> _s.update { it.copy(uploadingPhoto = false, photoUrl = url) } },
        onFail = { err -> _s.update { it.copy(uploadingPhoto = false, error = err) } },
        call = { id, u -> workers.uploadPhoto(id, u) },
    )

    fun uploadAadhaarFront(uri: Uri) = upload(uri,
        onStart = { _s.update { it.copy(uploadingAadhaarFront = true, error = null) } },
        onDone = { url -> _s.update { it.copy(uploadingAadhaarFront = false, aadhaarFrontUrl = url) } },
        onFail = { err -> _s.update { it.copy(uploadingAadhaarFront = false, error = err) } },
        call = { id, u -> workers.uploadAadhaarFront(id, u) },
    )

    fun uploadAadhaarBack(uri: Uri) = upload(uri,
        onStart = { _s.update { it.copy(uploadingAadhaarBack = true, error = null) } },
        onDone = { url -> _s.update { it.copy(uploadingAadhaarBack = false, aadhaarBackUrl = url) } },
        onFail = { err -> _s.update { it.copy(uploadingAadhaarBack = false, error = err) } },
        call = { id, u -> workers.uploadAadhaarBack(id, u) },
    )

    private fun upload(uri: Uri, onStart: () -> Unit, onDone: (String) -> Unit, onFail: (String) -> Unit, call: suspend (String, Uri) -> String) {
        val id = _s.value.workerId
        if (id == null) { onFail("Save the worker first to upload"); return }
        onStart()
        viewModelScope.launch {
            try { onDone(call(id, uri)) } catch (e: Throwable) { onFail(e.message ?: "Upload failed") }
        }
    }

    fun recordSalary(amount: Double, payForMonth: String, method: String, reference: String?) {
        val id = _s.value.workerId ?: return
        _s.update { it.copy(recordingSalary = true, error = null) }
        viewModelScope.launch {
            try {
                workers.recordSalary(id, amount, payForMonth, method, reference, null)
                val sal = workers.listSalary(workerId = id)
                _s.update { it.copy(recordingSalary = false, showSalaryDialog = false, salaryPayments = sal, message = "Salary recorded") }
            } catch (e: Throwable) {
                _s.update { it.copy(recordingSalary = false, error = e.message ?: "Failed") }
            }
        }
    }
}
