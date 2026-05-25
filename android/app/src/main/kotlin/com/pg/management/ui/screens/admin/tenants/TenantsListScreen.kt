package com.pg.management.ui.screens.admin.tenants

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pg.management.domain.model.Tenant
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import kotlinx.coroutines.delay

@Composable
fun TenantsListScreen(
    onTenantClick: (String) -> Unit,
    onAddTenant: () -> Unit,
    onShowInfo: (String) -> Unit,
    vm: TenantsListViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    LaunchedEffect(s.query) {
        delay(350)
        vm.refresh()
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTenant,
                containerColor = BrandPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Add Member", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Members", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = s.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search name, code, phone…", color = Slate400) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Slate400) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("active" to "Active", "all" to "All", "left" to "Left").forEach { (v, label) ->
                    FilterChip(
                        selected = s.statusFilter == v,
                        onClick = { vm.onStatusFilter(v) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandCyan.copy(alpha = 0.15f),
                            selectedLabelColor = BrandCyan,
                            containerColor = Color.White.copy(alpha = 0.06f),
                            labelColor = Slate200,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            when {
                s.loading && s.tenants.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandCyan)
                    }
                }
                s.tenants.isEmpty() && s.error == null -> {
                    EmptyState("No members yet. Tap '+ Add Member' to create the first one.")
                }
                s.error != null -> EmptyState(s.error!!, isError = true)
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.tenants, key = { it.id }) { t ->
                        TenantRow(t, onClick = { onTenantClick(t.id) }, onInfo = { onShowInfo(t.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TenantRow(t: Tenant, onClick: () -> Unit, onInfo: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            if (t.photoUrl != null) {
                AsyncImage(model = t.photoUrl, contentDescription = null, modifier = Modifier.size(48.dp), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Slate400)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.user.fullName, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(
                buildString {
                    append(t.user.userCode)
                    if (t.room != null) append(" · Room ${t.room.roomNumber}")
                    if (t.bed != null) append(" · Bed ${t.bed.bedLabel}")
                },
                color = Slate400,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        StatusChip(t.status)
        androidx.compose.material3.IconButton(onClick = onInfo) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Credentials",
                tint = com.pg.management.ui.theme.BrandCyan,
            )
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "active" -> "Active" to com.pg.management.ui.theme.Success
        "left" -> "Left" to com.pg.management.ui.theme.Slate400
        "inactive" -> "Inactive" to com.pg.management.ui.theme.Warning
        else -> status to Slate200
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyState(message: String, isError: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = if (isError) MaterialTheme.colorScheme.error else Slate400,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

