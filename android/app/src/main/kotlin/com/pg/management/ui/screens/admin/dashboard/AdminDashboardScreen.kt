package com.pg.management.ui.screens.admin.dashboard

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.BedroomParent
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.BrandPurple
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning

@Composable
fun AdminDashboardScreen(
    onSeeAllTenants: () -> Unit,
    onSeeAllRooms: () -> Unit,
    vm: AdminDashboardViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = "Dashboard",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Overview of your PG operations",
            color = Slate400,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))

        if (s.loading && s.stats.totalTenants == 0) {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandCyan)
            }
            return@Column
        }

        if (s.error != null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(s.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = vm::refresh) { Text("Retry", color = BrandCyan) }
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Active Tenants",
                value = s.stats.activeTenants.toString(),
                icon = Icons.Outlined.People,
                accent = BrandCyan,
                onClick = onSeeAllTenants,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Rooms",
                value = s.stats.totalRooms.toString(),
                icon = Icons.Outlined.Apartment,
                accent = BrandPurple,
                onClick = onSeeAllRooms,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Occupied Beds",
                value = s.stats.occupiedBeds.toString(),
                icon = Icons.Outlined.MeetingRoom,
                accent = Success,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Vacant Beds",
                value = s.stats.vacantBeds.toString(),
                icon = Icons.Outlined.BedroomParent,
                accent = Warning,
            )
        }
        Spacer(Modifier.height(24.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Occupancy",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            val total = (s.stats.occupiedBeds + s.stats.vacantBeds).coerceAtLeast(1)
            val occRatio = s.stats.occupiedBeds.toFloat() / total
            OccupancyBar(occRatio)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${s.stats.occupiedBeds} of ${s.stats.occupiedBeds + s.stats.vacantBeds} beds occupied",
                color = Slate200,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    GlassCard(
        modifier = modifier,
        padding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text(title, color = Slate400, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (onClick != null) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text("View all →", color = BrandCyan, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun OccupancyBar(ratio: Float) {
    val r = ratio.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.08f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(r)
                .height(10.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(BrandPrimary, BrandPurple, BrandCyan)
                    )
                ),
        )
    }
}
