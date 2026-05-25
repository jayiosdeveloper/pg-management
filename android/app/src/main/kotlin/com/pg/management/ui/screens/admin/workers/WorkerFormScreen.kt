package com.pg.management.ui.screens.admin.workers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pg.management.domain.model.SalaryPayment
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import java.time.LocalDate

private val GENDER_OPTIONS = listOf("Male", "Female", "Other")

@Composable
fun WorkerFormScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    vm: WorkerFormViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val isEdit = s.workerId != null
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(s.savedWorkerId, s.savedCredentials, isEdit) {
        if (s.savedWorkerId != null && s.savedCredentials == null && isEdit) {
            val id = s.savedWorkerId!!
            vm.acknowledgeSave()
            onSaved(id)
        }
    }
    LaunchedEffect(s.deleted) { if (s.deleted) onBack() }

    s.savedCredentials?.let { creds ->
        AlertDialog(
            onDismissRequest = { val id = s.savedWorkerId!!; vm.acknowledgeSave(); onSaved(id) },
            confirmButton = {
                TextButton(onClick = { val id = s.savedWorkerId!!; vm.acknowledgeSave(); onSaved(id) }) {
                    Text("Got it", color = BrandCyan)
                }
            },
            title = { Text("Worker created", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share these credentials with the worker. The password is shown only once.")
                    Spacer(Modifier.height(8.dp))
                    Text("User ID: ${creds.userCode}", fontWeight = FontWeight.SemiBold)
                    Text("Password: ${creds.tempPassword}", fontWeight = FontWeight.SemiBold)
                }
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete worker?", fontWeight = FontWeight.Bold) },
            text = { Text("This permanently deletes the worker, their login, and salary records.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete() }) { Text("Delete", color = Danger) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = Slate200) } },
        )
    }

    if (s.showSalaryDialog) {
        SalaryDialog(monthlyDefault = s.monthlySalary.toDoubleOrNull() ?: 0.0,
            submitting = s.recordingSalary, error = s.error,
            onDismiss = { vm.openSalary(false) },
            onSubmit = { amt, month, method, ref -> vm.recordSalary(amt, month, method, ref) })
    }

    s.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::consumeMessage,
            confirmButton = { TextButton(onClick = vm::consumeMessage) { Text("OK", color = BrandCyan) } },
            title = { Text("Done") },
            text = { Text(msg) },
        )
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(if (isEdit) "Edit Worker" else "Add Worker", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) } },
                    actions = { if (isEdit) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, null, tint = Danger) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {

                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Photos")
                    if (!isEdit) {
                        Text("Save first, then come back to upload photos.", color = Slate400, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(10.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarPicker(s.photoUrl, s.uploadingPhoto, enabled = isEdit, onPicked = vm::uploadPhoto)
                        Spacer(Modifier.size(16.dp))
                        Column {
                            Text("Profile photo", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(if (isEdit) "Tap to ${if (s.photoUrl == null) "upload" else "change"}" else "Available after saving", color = Slate400, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DocTile(Modifier.weight(1f), "Aadhaar front", s.aadhaarFrontUrl, s.uploadingAadhaarFront, enabled = isEdit, onPicked = vm::uploadAadhaarFront)
                        DocTile(Modifier.weight(1f), "Aadhaar back", s.aadhaarBackUrl, s.uploadingAadhaarBack, enabled = isEdit, onPicked = vm::uploadAadhaarBack)
                    }
                }
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Worker details")
                    Field("Full name *", s.fullName) { v -> vm.update { it.copy(fullName = v) } }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Email", s.email, Modifier.weight(1f)) { v -> vm.update { it.copy(email = v) } }
                        Field("Phone", s.phone, Modifier.weight(1f)) { v -> vm.update { it.copy(phone = v) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Role (Cook, Cleaner…)", s.roleTitle, Modifier.weight(1f)) { v -> vm.update { it.copy(roleTitle = v) } }
                        Field("Monthly salary ₹", s.monthlySalary, Modifier.weight(1f)) { v -> vm.update { it.copy(monthlySalary = v.filter { ch -> ch.isDigit() || ch == '.' }) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderDropdown(s.gender, Modifier.weight(1f)) { v -> vm.update { it.copy(gender = v) } }
                        Field("City", s.city, Modifier.weight(1f)) { v -> vm.update { it.copy(city = v) } }
                    }
                    Field("Address", s.address) { v -> vm.update { it.copy(address = v) } }
                }
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Aadhaar")
                    Field(
                        label = if (s.aadhaarWasSet) "Aadhaar number (locked)" else "Aadhaar number *",
                        value = s.idProofNumber,
                        enabled = !s.aadhaarWasSet,
                    ) { v -> vm.update { it.copy(idProofNumber = v) } }
                }
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Emergency contact")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Name", s.emergencyName, Modifier.weight(1f)) { v -> vm.update { it.copy(emergencyName = v) } }
                        Field("Phone", s.emergencyPhone, Modifier.weight(1f)) { v -> vm.update { it.copy(emergencyPhone = v) } }
                    }
                }
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Employment")
                    Field("Joining date (YYYY-MM-DD) *", s.joiningDate) { v -> vm.update { it.copy(joiningDate = v) } }
                    Field("Leaving date (optional)", s.leavingDate) { v -> vm.update { it.copy(leavingDate = v) } }
                    Field("Notes", s.notes) { v -> vm.update { it.copy(notes = v) } }
                }
                Spacer(Modifier.height(12.dp))

                if (isEdit) {
                    GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Salary payments", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            TextButton(onClick = { vm.openSalary(true) }) { Text("+ Record", color = BrandCyan) }
                        }
                        Spacer(Modifier.height(6.dp))
                        if (s.salaryPayments.isEmpty()) {
                            Text("No salary payments recorded yet.", color = Slate400, style = MaterialTheme.typography.bodySmall)
                        } else {
                            s.salaryPayments.forEach { p -> SalaryRow(p) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (s.error != null) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                PrimaryButton(
                    text = if (isEdit) "Save changes" else "Create worker",
                    onClick = vm::submit,
                    loading = s.saving,
                    enabled = s.canSubmit,
                )
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun Field(label: String, value: String, modifier: Modifier = Modifier, enabled: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text(label) }, singleLine = true, enabled = enabled,
        shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
    )
}

@Composable
private fun GenderDropdown(value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = value.ifBlank { "Select" }, onValueChange = {}, readOnly = true,
            label = { Text("Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("(none)") }, onClick = { onChange(""); expanded = false })
            GENDER_OPTIONS.forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { onChange(g); expanded = false }) }
        }
    }
}

@Composable
private fun AvatarPicker(url: String?, uploading: Boolean, enabled: Boolean, onPicked: (android.net.Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(onPicked) }
    Box(
        modifier = Modifier
            .size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
            .let { if (enabled && !uploading) it.clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } else it },
        contentAlignment = Alignment.Center,
    ) {
        when {
            uploading -> CircularProgressIndicator(color = BrandCyan, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            url != null -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(72.dp), contentScale = ContentScale.Crop)
            else -> Icon(Icons.Outlined.Engineering, null, tint = Slate400, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun DocTile(modifier: Modifier, label: String, url: String?, uploading: Boolean, enabled: Boolean, onPicked: (android.net.Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(onPicked) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.08f))
                .let { if (enabled && !uploading) it.clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } else it },
            contentAlignment = Alignment.Center,
        ) {
            when {
                uploading -> CircularProgressIndicator(color = BrandCyan, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                url != null -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddPhotoAlternate, null, tint = Slate400)
                    Text(if (enabled) "Upload" else "After saving", color = Slate400, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Slate200, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SalaryRow(p: SalaryPayment) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(p.payForMonth.take(7), color = Slate200, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(p.method.replaceFirstChar { it.uppercase() }, color = Slate400, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text("₹ %.0f".format(p.amount), color = Success, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SalaryDialog(monthlyDefault: Double, submitting: Boolean, error: String?, onDismiss: () -> Unit, onSubmit: (Double, String, String, String?) -> Unit) {
    var amount by remember { mutableStateOf(if (monthlyDefault > 0) monthlyDefault.toInt().toString() else "") }
    var month by remember { mutableStateOf(LocalDate.now().toString().take(7)) }
    var method by remember { mutableStateOf("cash") }
    var reference by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record salary payment", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Amount") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                    OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("YYYY-MM") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("cash", "upi", "bank_transfer", "card", "other").forEach { m ->
                        TextButton(onClick = { method = m }) { Text(m, color = if (m == method) BrandCyan else Slate400, style = MaterialTheme.typography.bodySmall) }
                    }
                }
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Reference (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                if (error != null) Text(error, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !submitting && (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = { onSubmit(amount.toDouble(), month.trim(), method, reference.trim().ifBlank { null }) },
            ) { Text(if (submitting) "Saving…" else "Save", color = BrandCyan) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Slate200) } },
    )
}
