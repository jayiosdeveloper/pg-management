package com.pg.management.ui.screens.admin.electricity

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.domain.model.ElectricityReading
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import java.time.YearMonth

@Composable
fun ElectricityScreen(
    onBack: () -> Unit,
    vm: ElectricityViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    if (s.showForm) AddReadingDialog(s, vm)

    s.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::consumeMessage,
            confirmButton = { TextButton(onClick = vm::consumeMessage) { Text("OK", color = BrandCyan) } },
            title = { Text("Done", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
        )
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Electricity", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { vm.openForm(true) },
                    containerColor = BrandPrimary, contentColor = Color.White,
                    icon = { Icon(Icons.Outlined.Add, null) },
                    text = { Text("Add reading", fontWeight = FontWeight.SemiBold) },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(padding).padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                MonthPicker(s.month, vm::setMonth)
                Spacer(Modifier.height(12.dp))

                when {
                    s.loading && s.readings.isEmpty() -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandCyan)
                    }
                    s.readings.isEmpty() -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        Text("No electricity readings for ${s.month}. Tap 'Add reading' to enter meter values.", color = Slate400, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    s.error != null -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(s.readings, key = { it.id }) { r -> ReadingCard(r, deleting = s.deletingId == r.id, onDelete = { vm.deleteReading(r.id) }) }
                        item { Spacer(Modifier.height(60.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(month: String, onChange: (String) -> Unit) {
    val ym = remember(month) { runCatching { YearMonth.parse(month) }.getOrDefault(YearMonth.now()) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(ym.minusMonths(1).toString()) }) { Icon(Icons.Outlined.ChevronLeft, null, tint = Color.White) }
        Box(
            Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(month, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { onChange(ym.plusMonths(1).toString()) }) { Icon(Icons.Outlined.ChevronRight, null, tint = Color.White) }
    }
}

@Composable
private fun ReadingCard(r: ElectricityReading, deleting: Boolean, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete reading?", fontWeight = FontWeight.Bold) },
            text = { Text("Removes this reading and any unpaid electricity bills created from it. Bills that already have payments will be kept.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete", color = Danger) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = Slate200) } },
        )
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Room ${r.roomNumber.orEmpty()}", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    "${"%.0f".format(r.startReading)} → ${"%.0f".format(r.endReading)} = ${"%.0f".format(r.unitsUsed)} units × ₹${"%.0f".format(r.ratePerUnit)}",
                    color = Slate400, style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Total ₹${"%.0f".format(r.totalAmount)} · Per member ₹${"%.0f".format(r.perMemberAmount)} (${r.memberCount} members)",
                    color = Slate200, style = MaterialTheme.typography.bodySmall,
                )
                if (!r.notes.isNullOrBlank()) Text("Note: ${r.notes}", color = Slate400, style = MaterialTheme.typography.bodySmall)
            }
            if (deleting) CircularProgressIndicator(color = Danger, strokeWidth = 2.dp, modifier = Modifier.padding(4.dp))
            else IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, null, tint = Danger) }
        }
    }
}

@Composable
private fun AddReadingDialog(s: ElectricityUi, vm: ElectricityViewModel) {
    var roomExpanded by remember { mutableStateOf(false) }
    val selectedRoom = s.rooms.firstOrNull { it.id == s.formRoomId }

    AlertDialog(
        onDismissRequest = { vm.openForm(false) },
        title = { Text("Add electricity reading", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Month: ${s.month}", color = Slate200, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = roomExpanded, onExpandedChange = { roomExpanded = !roomExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        value = selectedRoom?.let { "Room ${it.roomNumber} (${it.occupiedCount}/${it.capacity})" } ?: "Select room",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Room") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                        shape = RoundedCornerShape(10.dp), colors = textFieldColors(),
                    )
                    ExposedDropdownMenu(expanded = roomExpanded, onDismissRequest = { roomExpanded = false }) {
                        s.rooms.filter { it.occupiedCount > 0 }.forEach { r ->
                            DropdownMenuItem(
                                text = { Text("Room ${r.roomNumber} · ${r.occupiedCount} member${if (r.occupiedCount > 1) "s" else ""}") },
                                onClick = { vm.update { it.copy(formRoomId = r.id) }; roomExpanded = false },
                            )
                        }
                        if (s.rooms.none { it.occupiedCount > 0 }) {
                            DropdownMenuItem(text = { Text("No occupied rooms available") }, onClick = { roomExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = s.formStartReading, onValueChange = { v -> vm.update { it.copy(formStartReading = v.filter { ch -> ch.isDigit() || ch == '.' }) } },
                        label = { Text("Start reading") }, modifier = Modifier.weight(1f), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp), colors = textFieldColors(),
                    )
                    OutlinedTextField(
                        value = s.formEndReading, onValueChange = { v -> vm.update { it.copy(formEndReading = v.filter { ch -> ch.isDigit() || ch == '.' }) } },
                        label = { Text("End reading") }, modifier = Modifier.weight(1f), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp), colors = textFieldColors(),
                    )
                }
                OutlinedTextField(
                    value = s.formRatePerUnit, onValueChange = { v -> vm.update { it.copy(formRatePerUnit = v.filter { ch -> ch.isDigit() || ch == '.' }) } },
                    label = { Text("Rate per unit (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = textFieldColors(),
                )
                OutlinedTextField(
                    value = s.formNotes, onValueChange = { v -> vm.update { it.copy(formNotes = v) } },
                    label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), colors = textFieldColors(),
                )

                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BrandCyan.copy(alpha = 0.12f)).padding(12.dp),
                ) {
                    Column {
                        Text("Preview", color = BrandCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Units used: ${"%.0f".format(s.previewUnits)}",
                            color = Color.White, style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "Total: ₹${"%.0f".format(s.previewTotal)}",
                            color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Members in room: ${s.previewMemberCount} → each pays ₹${"%.0f".format(s.previewPerMember)}",
                            color = Slate200, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (s.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(s.error!!, color = Danger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = s.canSubmit, onClick = vm::submitReading) {
                Text(if (s.submitting) "Saving…" else "Generate bills", color = BrandCyan, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = { vm.openForm(false) }) { Text("Cancel", color = Slate200) } },
    )
}
