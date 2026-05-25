package com.pg.management.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.screens.session.SessionViewModel
import com.pg.management.ui.theme.Slate200

@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    vm: SessionViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    LaunchedEffect(s.loggedOut) {
        if (s.loggedOut) {
            vm.consumeLogout()
            onLogout()
        }
    }

    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Admin Dashboard",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Hello, ${s.session?.fullName ?: ""}",
                    color = Slate200,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Phase 2 ready",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Auth + navigation are wired up. Tenant management, billing, PDF and notifications arrive in phases 3 through 8.",
                        color = Slate200,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                    PrimaryButton(text = "Logout", onClick = vm::logout, loading = s.loggingOut)
                }
            }
        }
    }
}
