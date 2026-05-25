package com.pg.management.ui.screens.admin.tenants

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
import androidx.compose.material.icons.outlined.Person
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
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400

private val GENDER_OPTIONS = listOf("Male", "Female", "Other")

@Composable
fun TenantFormScreen(
    onBack: () -> Unit,
    onSaved: (tenantId: String) -> Unit,
    vm: TenantFormViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val isEdit = s.tenantId != null

    // After EDIT save (no credentials dialog) -> navigate back automatically.
    LaunchedEffect(s.savedTenantId, s.savedCredentials, isEdit) {
        if (s.savedTenantId != null && s.savedCredentials == null && isEdit) {
            val id = s.savedTenantId!!
            vm.acknowledgeSave()
            onSaved(id)
        }
    }

    s.savedCredentials?.let { creds ->
        AlertDialog(
            onDismissRequest = {
                val id = s.savedTenantId!!
                vm.acknowledgeSave()
                onSaved(id)
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = s.savedTenantId!!
                    vm.acknowledgeSave()
                    onSaved(id)
                }) { Text("Got it", color = BrandCyan) }
            },
            title = { Text("Member created", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share these credentials with the member. The password is shown only once.")
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
                    title = { Text(if (isEdit) "Edit Member" else "Add Member", color = Color.White) },
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
                // Photos — only enabled once member exists
                GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                    SectionTitle("Photos")
                    if (!isEdit) {
                        Text(
                            "Save first, then come back to upload photos.",
                            color = Slate400,
                            style = MaterialTheme.typography.bodySmall,
                        )
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
                    SectionTitle("Personal details")
                    Field("Full name *", s.fullName) { v -> vm.update { it.copy(fullName = v) } }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Email", s.email, Modifier.weight(1f)) { v -> vm.update { it.copy(email = v) } }
                        Field("Phone", s.phone, Modifier.weight(1f)) { v -> vm.update { it.copy(phone = v) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderDropdown(s.gender, Modifier.weight(1f)) { v -> vm.update { it.copy(gender = v) } }
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
                    SectionTitle("Aadhaar")
                    Field(
                        label = if (s.aadhaarWasSet) "Aadhaar number (locked)" else "Aadhaar number *",
                        value = s.idProofNumber,
                        enabled = !s.aadhaarWasSet,
                    ) { v -> vm.update { it.copy(idProofNumber = v) } }
                    if (s.aadhaarWasSet) {
                        Text(
                            "Aadhaar number cannot be changed once saved.",
                            color = Slate400,
                            style = MaterialTheme.typography.bodySmall,
                        )
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
                    Field("Joining date (YYYY-MM-DD) *", s.joiningDate) { v -> vm.update { it.copy(joiningDate = v) } }
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
                    text = if (isEdit) "Save changes" else "Create member",
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
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors(),
    )
}

@Composable
private fun GenderDropdown(value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = value.ifBlank { "Select" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("(none)") }, onClick = { onChange(""); expanded = false })
            GENDER_OPTIONS.forEach { g ->
                DropdownMenuItem(text = { Text(g) }, onClick = { onChange(g); expanded = false })
            }
        }
    }
}

@Composable
private fun RoomBedPicker(s: TenantFormUi, vm: TenantFormViewModel) {
    var roomExpanded by remember { mutableStateOf(false) }
    var bedExpanded by remember { mutableStateOf(false) }
    val selectedRoom = s.rooms.firstOrNull { it.id == s.roomId }
    val availableBeds = selectedRoom?.beds?.filter { it.status != "occupied" || it.id == s.bedId }.orEmpty()

    ExposedDropdownMenuBox(expanded = roomExpanded, onExpandedChange = { roomExpanded = !roomExpanded }) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor().padding(vertical = 4.dp),
            value = selectedRoom?.let { "Room ${it.roomNumber}" } ?: "No room assigned",
            onValueChange = {}, readOnly = true,
            label = { Text("Room") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
            shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
        )
        ExposedDropdownMenu(expanded = roomExpanded, onDismissRequest = { roomExpanded = false }) {
            DropdownMenuItem(text = { Text("(no room)") }, onClick = { vm.update { it.copy(roomId = null, bedId = null) }; roomExpanded = false })
            s.rooms.forEach { r ->
                DropdownMenuItem(
                    text = { Text("Room ${r.roomNumber} · ${r.vacantCount}/${r.capacity} free") },
                    onClick = { vm.update { it.copy(roomId = r.id, bedId = null) }; roomExpanded = false },
                )
            }
        }
    }

    if (selectedRoom != null) {
        ExposedDropdownMenuBox(expanded = bedExpanded, onExpandedChange = { bedExpanded = !bedExpanded }) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().menuAnchor().padding(vertical = 4.dp),
                value = availableBeds.firstOrNull { it.id == s.bedId }?.let { "Bed ${it.bedLabel}" } ?: "No bed",
                onValueChange = {}, readOnly = true,
                label = { Text("Bed") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bedExpanded) },
                shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
            )
            ExposedDropdownMenu(expanded = bedExpanded, onDismissRequest = { bedExpanded = false }) {
                DropdownMenuItem(text = { Text("(no bed)") }, onClick = { vm.update { it.copy(bedId = null) }; bedExpanded = false })
                availableBeds.forEach { b ->
                    DropdownMenuItem(text = { Text("Bed ${b.bedLabel} · ${b.status}") }, onClick = { vm.update { it.copy(bedId = b.id) }; bedExpanded = false })
                }
            }
        }
    }
}

@Composable
private fun AvatarPicker(url: String?, uploading: Boolean, enabled: Boolean, onPicked: (android.net.Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(onPicked) }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .let { if (enabled && !uploading) it.clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } else it },
        contentAlignment = Alignment.Center,
    ) {
        when {
            uploading -> CircularProgressIndicator(color = BrandCyan, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            url != null -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(72.dp), contentScale = ContentScale.Crop)
            else -> Icon(Icons.Outlined.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun DocTile(modifier: Modifier = Modifier, label: String, url: String?, uploading: Boolean, enabled: Boolean, onPicked: (android.net.Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(onPicked) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
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

// Shared with other screens
@Composable
internal fun textFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Slate400,
    focusedBorderColor = BrandCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    disabledBorderColor = Color.White.copy(alpha = 0.1f),
    focusedLabelColor = BrandCyan,
    unfocusedLabelColor = Slate400,
    disabledLabelColor = Slate400,
    cursorColor = BrandCyan,
    focusedContainerColor = Color.White.copy(alpha = 0.04f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
    disabledContainerColor = Color.White.copy(alpha = 0.02f),
)
