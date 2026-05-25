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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.BuildConfig
import com.pg.management.domain.model.AppNotification
import com.pg.management.domain.model.Bill
import com.pg.management.domain.model.Complaint
import com.pg.management.domain.model.Invoice
import com.pg.management.domain.model.Payment
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
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
            NavigationBar(containerColor = com.pg.management.ui.theme.BrandDeepDarker.copy(alpha = 0.9f)) {
                val tabs = remember {
                    listOf(
                        "Home" to Icons.Outlined.Apartment,
                        "Bills" to Icons.Outlined.Payments,
                        "Invoices" to Icons.Outlined.Description,
                        "Notices" to Icons.Outlined.Notifications,
                        "Issues" to Icons.Outlined.ReportProblem,
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
                    0 -> TenantHomeTab(s, vm::logout)
                    1 -> BillsTab(s.bills, s.payments)
                    2 -> InvoicesTab(s.invoices)
                    3 -> NoticesTab(s.notifications, vm::markNotifRead, vm::markAllNotifsRead)
                    4 -> ComplaintsTab(s, vm)
                }
            }
        }
    }
}

@Composable
private fun TenantHomeTab(s: TenantHomeUi, onLogout: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
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

            Spacer(Modifier.height(20.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Recent activity", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (s.payments.isEmpty()) {
                    Text("No payments yet.", color = Slate400, style = MaterialTheme.typography.bodySmall)
                } else {
                    s.payments.take(5).forEach { p ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(p.paidAt.take(10), color = Slate400, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text(p.method.replaceFirstChar { it.uppercase() }, color = Slate200, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text("₹ %.0f".format(p.amount), color = Success, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(bills, key = { it.id }) { b ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${b.category.replaceFirstChar { it.uppercase() }} · ${b.billingMonth.take(7)}", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Due ${b.dueDate}", color = Slate400, style = MaterialTheme.typography.bodySmall)
                            if (!b.description.isNullOrBlank()) Text(b.description, color = Slate200, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹ %.0f".format(b.amount), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val (label, color) = when (b.status) {
                                "paid" -> "Paid" to Success
                                "partial" -> "Partial · ₹%.0f left".format(b.pending) to Warning
                                "overdue" -> "Overdue" to Danger
                                else -> "Unpaid" to Slate400
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(label, color = color, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun InvoicesTab(invoices: List<Invoice>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Text("Invoices", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        Spacer(Modifier.height(12.dp))
        if (invoices.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text("No invoices yet. Admin can generate one from the dashboard.", color = Slate400)
            }
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(invoices, key = { it.id }) { inv ->
                GlassCard(modifier = Modifier.fillMaxWidth().clickable(enabled = inv.pdfUrl != null) {
                    inv.pdfUrl?.let { url ->
                        val i = android.content.Intent(android.content.Intent.ACTION_VIEW, url.toUri())
                        i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)
                    }
                }) {
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(inv.invoiceNumber, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Month: ${inv.billingMonth.take(7)}", color = Slate400, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(4.dp))
                            Text("Total ₹%.0f · Paid ₹%.0f · Pending ₹%.0f".format(inv.totalAmount, inv.paidAmount, inv.pendingAmount), color = Slate200, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(if (inv.pdfUrl != null) "Open PDF →" else "—", color = BrandCyan, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun NoticesTab(items: List<AppNotification>, markRead: (String) -> Unit, markAll: () -> Unit) {
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notices", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), modifier = Modifier.weight(1f))
            if (items.any { !it.isRead }) {
                TextButton(onClick = markAll) { Text("Mark all read", color = BrandCyan) }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text("No notices yet.", color = Slate400)
            }
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { n ->
                GlassCard(modifier = Modifier.fillMaxWidth().clickable { if (!n.isRead) markRead(n.id) }) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (n.isRead) Color.Transparent else BrandCyan))
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

@Composable
private fun ComplaintsTab(s: TenantHomeUi, vm: TenantHomeViewModel) {
    var showForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }

    LaunchedEffect(s.complaintSubmitted) {
        if (s.complaintSubmitted) {
            showForm = false; title = ""; description = ""; priority = "medium"
            vm.consumeComplaintSubmitted()
        }
    }

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Issues", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), modifier = Modifier.weight(1f))
            TextButton(onClick = { showForm = true }) { Text("+ New", color = BrandCyan) }
        }
        Spacer(Modifier.height(8.dp))

        if (s.complaints.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text("No complaints submitted yet.", color = Slate400)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(s.complaints, key = { it.id }) { c -> ComplaintRow(c) }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }

    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("New issue", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                    Spacer(Modifier.height(8.dp))
                    Text("Priority: $priority", color = Slate200, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("low", "medium", "high", "urgent").forEach { p ->
                            TextButton(onClick = { priority = p }) { Text(p, color = if (p == priority) BrandCyan else Slate400) }
                        }
                    }
                    if (s.error != null) Text(s.error, color = Danger, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = title.isNotBlank() && description.isNotBlank() && !s.submittingComplaint,
                    onClick = { vm.submitComplaint(title.trim(), description.trim(), priority) },
                ) { Text(if (s.submittingComplaint) "Submitting…" else "Submit", color = BrandCyan) }
            },
            dismissButton = { TextButton(onClick = { showForm = false }) { Text("Cancel", color = Slate200) } },
        )
    }
}

@Composable
private fun ComplaintRow(c: Complaint) {
    val color = when (c.status) {
        "open" -> Warning
        "in_progress" -> BrandCyan
        "resolved" -> Success
        "closed" -> Slate400
        else -> Slate200
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row {
            Column(Modifier.weight(1f)) {
                Text(c.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(c.description, color = Slate200, style = MaterialTheme.typography.bodySmall)
                if (!c.adminResponse.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Admin: ${c.adminResponse}", color = BrandCyan, style = MaterialTheme.typography.bodySmall)
                }
                Text("${c.priority.uppercase()} · ${c.createdAt.take(10)}", color = Slate400, style = MaterialTheme.typography.labelSmall)
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(c.status.replace('_', ' ').replaceFirstChar { it.uppercase() }, color = color, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
