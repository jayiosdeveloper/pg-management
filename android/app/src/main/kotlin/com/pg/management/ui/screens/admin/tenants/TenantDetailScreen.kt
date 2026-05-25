package com.pg.management.ui.screens.admin.tenants

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.pg.management.domain.model.Tenant
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400

@Composable
fun TenantDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    vm: TenantDetailViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    LaunchedEffect(s.deleted) { if (s.deleted) onDeleted() }

    var confirmDelete by remember { mutableStateOf(false) }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete tenant?", fontWeight = FontWeight.Bold) },
            text = { Text("This permanently deletes the tenant, their login, and uploaded documents. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.delete() }) { Text("Delete", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = Slate200) } },
        )
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Tenant", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) }
                    },
                    actions = {
                        s.tenant?.let {
                            IconButton(onClick = { onEdit(it.id) }) { Icon(Icons.Outlined.Edit, null, tint = Color.White) }
                            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, null, tint = Danger) }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(padding),
            ) {
                when {
                    s.loading && s.tenant == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandCyan)
                    }
                    s.tenant != null -> TenantBody(s.tenant!!, s, vm)
                    s.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantBody(t: Tenant, s: TenantDetailUi, vm: TenantDetailViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Header
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarUpload(t.photoUrl, isUploading = s.uploading == UploadKind.PHOTO) { uri -> vm.upload(UploadKind.PHOTO, uri) }
                Spacer(Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(t.user.fullName, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("ID: ${t.user.userCode}", color = Slate200, style = MaterialTheme.typography.bodyMedium)
                    if (t.room != null) Text("Room ${t.room.roomNumber}${t.bed?.let { " · Bed ${it.bedLabel}" }.orEmpty()}", color = Slate400, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        InfoCard("Contact", listOf(
            "Phone" to t.user.phone,
            "Email" to t.user.email,
            "Emergency name" to t.emergencyContactName,
            "Emergency phone" to t.emergencyContactPhone,
        ))
        Spacer(Modifier.height(12.dp))
        InfoCard("Personal", listOf(
            "Gender" to t.gender,
            "DOB" to t.dateOfBirth,
            "Occupation" to t.occupation,
            "City" to t.city,
            "State" to t.state,
            "Address" to t.address,
        ))
        Spacer(Modifier.height(12.dp))
        InfoCard("Stay", listOf(
            "Joining" to t.joiningDate,
            "Leaving" to t.leavingDate,
            "Monthly rent" to t.monthlyRent?.let { "₹ %.0f".format(it) },
            "Security deposit" to t.securityDeposit?.let { "₹ %.0f".format(it) },
            "Notes" to t.notes,
        ))
        Spacer(Modifier.height(12.dp))

        DocumentsCard(t, s, vm)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun InfoCard(title: String, items: List<Pair<String, String?>>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        items.filter { !it.second.isNullOrBlank() }.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(label, color = Slate400, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(value!!, color = Slate200, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f))
            }
        }
        if (items.none { !it.second.isNullOrBlank() }) {
            Text("Not provided", color = Slate400, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DocumentsCard(t: Tenant, s: TenantDetailUi, vm: TenantDetailViewModel) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text("Documents", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DocumentTile(label = "Aadhaar front", url = t.aadhaarFrontUrl, uploading = s.uploading == UploadKind.AADHAAR_FRONT, modifier = Modifier.weight(1f)) {
                vm.upload(UploadKind.AADHAAR_FRONT, it)
            }
            DocumentTile(label = "Aadhaar back", url = t.aadhaarBackUrl, uploading = s.uploading == UploadKind.AADHAAR_BACK, modifier = Modifier.weight(1f)) {
                vm.upload(UploadKind.AADHAAR_BACK, it)
            }
        }
        if (s.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(s.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AvatarUpload(url: String?, isUploading: Boolean, onPicked: (android.net.Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(onPicked) }
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(enabled = !isUploading) {
                picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            isUploading -> CircularProgressIndicator(color = BrandCyan, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            url != null -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(82.dp), contentScale = ContentScale.Crop)
            else -> Icon(Icons.Outlined.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun DocumentTile(label: String, url: String?, uploading: Boolean, modifier: Modifier = Modifier, onPicked: (android.net.Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(onPicked) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable(enabled = !uploading) {
                    picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                uploading -> CircularProgressIndicator(color = BrandCyan, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                url != null -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddPhotoAlternate, null, tint = Slate400)
                    Text("Upload", color = Slate400, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Slate200, style = MaterialTheme.typography.bodySmall)
    }
}
