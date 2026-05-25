package com.pg.management.ui.screens.admin.more

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pg.management.domain.auth.AuthSession
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400

@Composable
fun AdminMoreScreen(
    onLogout: () -> Unit,
    loggingOut: Boolean,
    session: AuthSession?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text("Settings", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        Spacer(Modifier.height(20.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(session?.fullName ?: "—", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(session?.email ?: session?.userCode.orEmpty(), color = Slate200, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text("Role: ${session?.role?.value ?: "—"}", color = Slate400, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            PrimaryButton(text = "Sign out", onClick = onLogout, loading = loggingOut)
        }
    }
}
