package com.pg.management.ui.screens.admin.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.pg.management.domain.model.Worker
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning
import kotlinx.coroutines.delay

@Composable
fun WorkersListScreen(
    onWorkerClick: (String) -> Unit,
    onAddWorker: () -> Unit,
    onShowInfo: (String) -> Unit,
    vm: WorkersListViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    LaunchedEffect(s.query) { delay(350); vm.refresh() }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddWorker,
                containerColor = BrandPrimary, contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Add Worker", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("Workers", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = s.query, onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search name, code, role…", color = Slate400) },
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
                s.loading && s.workers.isEmpty() -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandCyan)
                }
                s.workers.isEmpty() && s.error == null -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text("No workers yet. Tap '+ Add Worker' to create the first one.", color = Slate400, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                }
                s.error != null -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.workers, key = { it.id }) { w -> WorkerRow(w, onClick = { onWorkerClick(w.id) }, onInfo = { onShowInfo(w.id) }) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkerRow(w: Worker, onClick: () -> Unit, onInfo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
            if (w.photoUrl != null) {
                AsyncImage(model = w.photoUrl, contentDescription = null, modifier = Modifier.size(48.dp), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Outlined.Engineering, null, tint = Slate400)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(w.user.fullName, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                buildString {
                    append(w.user.userCode)
                    if (!w.roleTitle.isNullOrBlank()) append(" · ${w.roleTitle}")
                    if (w.monthlySalary > 0) append(" · ₹${"%.0f".format(w.monthlySalary)}/mo")
                },
                color = Slate400, style = MaterialTheme.typography.bodySmall,
            )
        }
        val (label, color) = when (w.status) {
            "active" -> "Active" to Success
            "left" -> "Left" to Slate400
            "inactive" -> "Inactive" to Warning
            else -> w.status to Slate200
        }
        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = onInfo) { Icon(Icons.Outlined.Info, null, tint = BrandCyan) }
    }
}
