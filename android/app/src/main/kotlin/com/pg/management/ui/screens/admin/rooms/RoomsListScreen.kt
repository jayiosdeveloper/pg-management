package com.pg.management.ui.screens.admin.rooms

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.domain.model.Room
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning

@Composable
fun RoomsListScreen(
    onRoomClick: (String) -> Unit,
    onAddRoom: () -> Unit,
    vm: RoomsListViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRoom,
                containerColor = BrandPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Add Room", fontWeight = FontWeight.SemiBold) },
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
            Text("Rooms", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "All", "vacant" to "Vacant", "partial" to "Partial", "occupied" to "Full").forEach { (v, label) ->
                    FilterChip(
                        selected = s.filter == v,
                        onClick = { vm.onFilter(v) },
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
                s.loading && s.rooms.isEmpty() -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandCyan)
                }
                s.rooms.isEmpty() && s.error == null -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text("No rooms yet. Tap '+ Add Room' to create one.", color = Slate400, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                s.error != null -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.rooms, key = { it.id }) { r -> RoomRow(r, onClick = { onRoomClick(r.id) }) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RoomRow(r: Room, onClick: () -> Unit) {
    val accent = when (r.status) { "vacant" -> Success; "partial" -> Warning; "occupied" -> com.pg.management.ui.theme.Danger; else -> Slate400 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Room ${r.roomNumber}", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${r.occupiedCount}/${r.capacity} beds occupied · ₹ ${"%.0f".format(r.monthlyRent)}/mo${r.floor?.let { " · Floor $it" }.orEmpty()}",
                color = Slate400, style = MaterialTheme.typography.bodySmall,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(r.status.replaceFirstChar { it.uppercase() }, color = accent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}
