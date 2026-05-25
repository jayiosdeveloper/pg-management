package com.pg.management.ui.screens.admin.credentials

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.core.share.WhatsAppShare
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success

@Composable
fun MemberCredentialsScreen(
    onBack: () -> Unit,
    vm: MemberCredentialsViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Login credentials", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().systemBarsPadding().padding(padding)) {
                when {
                    s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandCyan)
                    }
                    s.info != null -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        val info = s.info!!
                        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(20.dp)) {
                            Text(info.fullName, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            CredentialRow("User ID", info.userCode, context)
                            info.email?.takeIf { it.isNotBlank() }?.let { CredentialRow("Email", it, context) }
                            info.phone?.takeIf { it.isNotBlank() }?.let { CredentialRow("Phone", it, context) }
                        }
                        Spacer(Modifier.height(12.dp))

                        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(20.dp)) {
                            Text("Password", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Existing passwords are stored hashed and cannot be read back. You can reset it below — a new password will appear and is shown only once.",
                                color = Slate400, style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(Modifier.height(12.dp))

                            if (s.newPassword != null) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Success.copy(alpha = 0.12f)).padding(14.dp),
                                ) {
                                    Column {
                                        Text("New password", color = Success, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(s.newPassword!!, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ActionChip("Copy", Icons.Outlined.ContentCopy) {
                                                copy(context, "Password", s.newPassword!!)
                                            }
                                            ActionChip("Send via WhatsApp", Icons.Outlined.ContentCopy) {
                                                val msg = buildString {
                                                    append("Hello ${info.fullName},\n")
                                                    append("Your PG Management login:\n")
                                                    append("User ID: ${info.userCode}\n")
                                                    info.email?.takeIf { it.isNotBlank() }?.let { append("Email: $it\n") }
                                                    append("Password: ${s.newPassword!!}")
                                                }
                                                WhatsAppShare.send(context, info.phone, msg)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            OutlinedTextField(
                                value = s.newPasswordInput,
                                onValueChange = vm::onNewPasswordInputChange,
                                label = { Text("Set a new password (optional)") },
                                placeholder = { Text("Leave empty to auto-generate", color = Slate400) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors(),
                            )
                            Spacer(Modifier.height(8.dp))
                            if (s.error != null) {
                                Text(s.error!!, color = Danger, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                            }
                            PrimaryButton(
                                text = if (s.newPasswordInput.isBlank()) "Auto-generate new password" else "Set this password",
                                onClick = { vm.reset(useAutoGen = s.newPasswordInput.isBlank()) },
                                loading = s.resetting,
                            )
                        }
                        Spacer(Modifier.height(40.dp))
                    }
                    s.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun CredentialRow(label: String, value: String, context: Context) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Slate400, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { copy(context, label, value) }) {
            Icon(Icons.Outlined.ContentCopy, null, tint = BrandCyan)
        }
    }
}

@Composable
private fun ActionChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrandCyan.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = BrandCyan, modifier = Modifier.height(16.dp))
        Text("  $text", color = BrandCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

private fun copy(context: Context, label: String, value: String) {
    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clip.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
}
