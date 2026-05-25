package com.pg.management.ui.screens.admin.tenants

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400

@Composable
fun TenantFormScreen(
    onBack: () -> Unit,
    onSaved: (tenantId: String) -> Unit,
    vm: TenantFormViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    s.savedCredentials?.let { creds ->
        AlertDialog(
            onDismissRequest = {
                vm.acknowledgeSave()
                onSaved(s.savedTenantId!!)
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.acknowledgeSave()
                    onSaved(s.savedTenantId!!)
                }) { Text("Got it", color = BrandCyan) }
            },
            title = { Text("Tenant created", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share these credentials with the tenant. The password is shown only once.")
                    Spacer(Modifier.height(8.dp))
                    Text("User ID: ${creds.userCode}", fontWeight = FontWeight.SemiBold)
                    Text("Password: ${creds.tempPassword}", fontWeight = FontWeight.SemiBold)
                }
            },
        )
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(if (s.tenantId == null) "Add Tenant" else "Edit Tenant", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Personal details")
                    Field("Full name *", s.fullName) { v -> vm.update { it.copy(fullName = v) } }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Email", s.email, Modifier.weight(1f)) { v -> vm.update { it.copy(email = v) } }
                        Field("Phone", s.phone, Modifier.weight(1f)) { v -> vm.update { it.copy(phone = v) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Gender", s.gender, Modifier.weight(1f)) { v -> vm.update { it.copy(gender = v) } }
                        Field("Occupation", s.occupation, Modifier.weight(1f)) { v -> vm.update { it.copy(occupation = v) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("City", s.city, Modifier.weight(1f)) { v -> vm.update { it.copy(city = v) } }
                        Field("State", s.state, Modifier.weight(1f)) { v -> vm.update { it.copy(state = v) } }
                    }
                    Field("Address", s.address) { v -> vm.update { it.copy(address = v) } }
                }
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("ID proof")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Type", s.idProofType, Modifier.weight(1f)) { v -> vm.update { it.copy(idProofType = v) } }
                        Field("Number", s.idProofNumber, Modifier.weight(2f)) { v -> vm.update { it.copy(idProofNumber = v) } }
                    }
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
                    SectionTitle("Accommodation")
                    RoomBedPicker(s, vm)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Joining date (YYYY-MM-DD) *", s.joiningDate, Modifier.weight(1f)) { v -> vm.update { it.copy(joiningDate = v) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Monthly rent", s.monthlyRent, Modifier.weight(1f)) { v -> vm.update { it.copy(monthlyRent = v) } }
                        Field("Security deposit", s.securityDeposit, Modifier.weight(1f)) { v -> vm.update { it.copy(securityDeposit = v) } }
                    }
                    Field("Notes", s.notes) { v -> vm.update { it.copy(notes = v) } }
                }
                Spacer(Modifier.height(16.dp))

                if (s.error != null) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                PrimaryButton(
                    text = if (s.tenantId == null) "Create tenant" else "Save changes",
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
private fun Field(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors(),
    )
}

@Composable
private fun RoomBedPicker(s: TenantFormUi, vm: TenantFormViewModel) {
    var roomExpanded by remember { mutableStateOf(false) }
    var bedExpanded by remember { mutableStateOf(false) }

    val selectedRoom = s.rooms.firstOrNull { it.id == s.roomId }
    val availableBeds = selectedRoom?.beds?.filter { it.status != "occupied" || it.id == s.bedId }.orEmpty()

    ExposedDropdownMenuBox(expanded = roomExpanded, onExpandedChange = { roomExpanded = !roomExpanded }) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(vertical = 4.dp),
            value = selectedRoom?.let { "Room ${it.roomNumber}" } ?: "No room assigned",
            onValueChange = {},
            readOnly = true,
            label = { Text("Room") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors(),
        )
        ExposedDropdownMenu(expanded = roomExpanded, onDismissRequest = { roomExpanded = false }) {
            DropdownMenuItem(text = { Text("(no room)") }, onClick = {
                vm.update { it.copy(roomId = null, bedId = null) }; roomExpanded = false
            })
            s.rooms.forEach { r ->
                DropdownMenuItem(
                    text = { Text("Room ${r.roomNumber} · ${r.status} · ${r.vacantCount}/${r.capacity} free") },
                    onClick = {
                        vm.update { it.copy(roomId = r.id, bedId = null) }; roomExpanded = false
                    },
                )
            }
        }
    }

    if (selectedRoom != null) {
        ExposedDropdownMenuBox(expanded = bedExpanded, onExpandedChange = { bedExpanded = !bedExpanded }) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(vertical = 4.dp),
                value = availableBeds.firstOrNull { it.id == s.bedId }?.let { "Bed ${it.bedLabel}" } ?: "No bed",
                onValueChange = {},
                readOnly = true,
                label = { Text("Bed") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bedExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
            )
            ExposedDropdownMenu(expanded = bedExpanded, onDismissRequest = { bedExpanded = false }) {
                DropdownMenuItem(text = { Text("(no bed)") }, onClick = { vm.update { it.copy(bedId = null) }; bedExpanded = false })
                availableBeds.forEach { b ->
                    DropdownMenuItem(
                        text = { Text("Bed ${b.bedLabel} · ${b.status}") },
                        onClick = { vm.update { it.copy(bedId = b.id) }; bedExpanded = false },
                    )
                }
            }
        }
    }
}
