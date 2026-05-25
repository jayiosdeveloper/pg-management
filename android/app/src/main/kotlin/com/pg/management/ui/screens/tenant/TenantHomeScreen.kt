package com.pg.management.ui.screens.tenant

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.domain.model.AppNotification
import com.pg.management.domain.model.Bill
import com.pg.management.domain.model.Payment
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandDeepDarker
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning

@Composable
fun TenantHomeScreen(
    onLogout: () -> Unit,
    vm: TenantHomeViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(s.loggedOut) { if (s.loggedOut) { vm.consumeLogout(); onLogout() } }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = BrandDeepDarker.copy(alpha = 0.9f)) {
                val tabs = remember {
                    listOf(
                        "Home" to Icons.Outlined.Apartment,
                        "Bills" to Icons.Outlined.Payments,
                        "Notifications" to Icons.Outlined.Notifications,
                    )
                }
                tabs.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan, unselectedIconColor = Slate400,
                            selectedTextColor = BrandCyan, unselectedTextColor = Slate400,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        GradientBackground {
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> HomeTab(s, onLogout = vm::logout)
                    1 -> BillsTab(s.bills, s.payments)
                    2 -> NoticesTab(s.notifications, vm::markNotifRead, vm::markAllNotifsRead)
                }
            }
        }
    }
}

@Composable
private fun HomeTab(s: TenantHomeUi, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
    ) {
        Text("Hello,", color = Slate400, style = MaterialTheme.typography.bodyMedium)
        Text(
            s.session?.fullName ?: "",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        )
        Spacer(Modifier.height(2.dp))
        Text("User ID: ${s.session?.userCode ?: "—"}", color = Slate400, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(18.dp))

        if (s.loading && s.bills.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandCyan)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox(modifier = Modifier.weight(1f), title = "Total Due", value = "₹ %.0f".format(s.summary.totalDue), accent = if (s.summary.totalDue > 0) Warning else Success)
                StatBox(modifier = Modifier.weight(1f), title = "Overdue", value = "₹ %.0f".format(s.summary.overdue), accent = if (s.summary.overdue > 0) Danger else Success)
            }
            Spacer(Modifier.height(12.dp))
            StatBox(modifier = Modifier.fillMaxWidth(), title = "Paid this month", value = "₹ %.0f".format(s.summary.paidThisMonth), accent = BrandCyan)

            Spacer(Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("My profile", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                InfoLine("Name", s.session?.fullName)
                InfoLine("User ID", s.session?.userCode)
                InfoLine("Email", s.session?.email)
            }
            Spacer(Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Bill overview", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                InfoLine("Total bills", s.summary.countBills.toString())
                InfoLine("Total due", "₹ %.0f".format(s.summary.totalDue))
                InfoLine("Overdue", "₹ %.0f".format(s.summary.overdue))
                InfoLine("Paid this month", "₹ %.0f".format(s.summary.paidThisMonth))
            }
            Spacer(Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Recent payments", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (s.payments.isEmpty()) {
                    Text("No payments yet.", color = Slate400, style = MaterialTheme.typography.bodySmall)
                } else {
                    s.payments.take(6).forEach { p -> PaymentLine(p) }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "Sign out", onClick = onLogout, loading = s.loggingOut)
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun StatBox(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, padding = PaddingValues(14.dp)) {
        Text(title, color = Slate400, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
        Text(value, color = accent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = Slate400, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, color = Slate200, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f))
    }
}

@Composable
private fun PaymentLine(p: Payment) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(p.paidAt.take(10), color = Slate400, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
        Text(p.method.replaceFirstChar { it.uppercase() }, color = Slate200, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text("₹ %.0f".format(p.amount), color = Success, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BillsTab(bills: List<Bill>, payments: List<Payment>) {
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Text("My Bills", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        Spacer(Modifier.height(12.dp))
        if (bills.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text("No bills yet.", color = Slate400)
            }
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(bills, key = { it.id }) { b -> BillCard(b, payments.filter { it.billId == b.id }) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun BillCard(b: Bill, billPayments: List<Payment>) {
    var expanded by remember { mutableStateOf(billPayments.isNotEmpty()) }
    GlassCard(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${b.category.replaceFirstChar { it.uppercase() }} · ${b.billingMonth.take(7)}", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Due ${b.dueDate}", color = Slate400, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹ %.0f".format(b.amount), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val (label, color) = when (b.status) {
                    "paid" -> "Full Paid" to Success
                    "partial" -> "Partial · ₹%.0f left".format(b.pending) to Warning
                    "overdue" -> "Overdue" to Danger
                    else -> "Unpaid" to Danger
                }
                Spacer(Modifier.height(2.dp))
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
            Spacer(Modifier.height(10.dp))
            Text("Payments", color = Slate200, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            if (billPayments.isEmpty()) {
                Text("No payments recorded yet.", color = Slate400, style = MaterialTheme.typography.bodySmall)
            } else {
                billPayments.sortedBy { it.paidAt }.forEach { p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Success))
                        Spacer(Modifier.size(8.dp))
                        Text(p.paidAt.take(16).replace('T', ' '), color = Slate400, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(p.method.replaceFirstChar { it.uppercase() }, color = Slate200, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.8f))
                        Text("₹ %.0f".format(p.amount), color = Success, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else if (billPayments.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("${billPayments.size} payment${if (billPayments.size > 1) "s" else ""} recorded — tap to view", color = BrandCyan, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NoticesTab(items: List<AppNotification>, markRead: (String) -> Unit, markAll: () -> Unit) {
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notifications", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), modifier = Modifier.weight(1f))
            if (items.any { !it.isRead }) {
                TextButton(onClick = markAll) { Text("Mark all read", color = BrandCyan) }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text("No notifications yet.", color = Slate400)
            }
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { n ->
                GlassCard(modifier = Modifier.fillMaxWidth().clickable { if (!n.isRead) markRead(n.id) }) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(if (n.isRead) Color.Transparent else BrandCyan))
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(n.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(n.body, color = Slate200, style = MaterialTheme.typography.bodyMedium)
                            Text(n.sentAt.take(16).replace('T', ' '), color = Slate400, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
