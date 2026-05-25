package com.pg.management.ui.screens.admin.rooms

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.screens.admin.tenants.textFieldColors

@Composable
fun RoomFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: RoomFormViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    LaunchedEffect(s.saved) { if (s.saved) onSaved() }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(if (s.roomId == null) "Add Room" else "Edit Room", color = Color.White) },
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
                    Text("Details", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = s.roomNumber, onValueChange = { v -> vm.update { it.copy(roomNumber = v) } },
                        label = { Text("Room number *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true, shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = s.floor, onValueChange = { v -> vm.update { it.copy(floor = v.filter { ch -> ch.isDigit() || ch == '-' }) } },
                            label = { Text("Floor") }, modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
                        )
                        OutlinedTextField(
                            value = s.capacity, onValueChange = { v -> vm.update { it.copy(capacity = v.filter { ch -> ch.isDigit() }) } },
                            label = { Text("Capacity *") }, modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
                        )
                    }
                    OutlinedTextField(
                        value = s.monthlyRent, onValueChange = { v -> vm.update { it.copy(monthlyRent = v.filter { ch -> ch.isDigit() || ch == '.' }) } },
                        label = { Text("Monthly rent (₹)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
                    )
                    OutlinedTextField(
                        value = s.description, onValueChange = { v -> vm.update { it.copy(description = v) } },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp), colors = textFieldColors(),
                    )
                }
                Spacer(Modifier.height(16.dp))
                if (s.error != null) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                PrimaryButton(
                    text = if (s.roomId == null) "Create room (beds auto-created)" else "Save changes",
                    onClick = vm::submit, loading = s.saving, enabled = s.canSubmit,
                )
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
