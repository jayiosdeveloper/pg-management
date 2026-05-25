package com.pg.management.ui.screens.admin.billing

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.core.share.WhatsAppShare
import com.pg.management.domain.model.Bill
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
fun BillDetailScreen(
    onBack: () -> Unit,
    vm: BillDetailViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(s.deleted) { if (s.deleted) onBack() }

    s.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::consumeMessage,
            confirmButton = { TextButton(onClick = vm::consumeMessage) { Text("OK", color = BrandCyan) } },
            title = { Text("Done") },
            text = { Text(msg) },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete bill?", fontWeight = FontWeight.Bold) },
            text = { Text("This permanently removes the bill and its payments.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete() }) { Text("Delete", color = Danger) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = Slate200) } },
        )
    }

    if (s.recordOpen) {
        s.bill?.let { b -> RecordPaymentDialog(b, s.recording, s.error, onDismiss = { vm.openRecord(false) }, onSubmit = vm::recordPayment) }
    }

    if (s.sharePromptVisible) {
        s.bill?.let { b ->
            val phone = s.tenantPhone
            val message = buildMessage(b, s.pdfUrl)
            AlertDialog(
                onDismissRequest = vm::dismissSharePrompt,
                title = { Text("Send invoice", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Share this bill's PDF with the member.", color = Slate200)
                        Spacer(Modifier.height(8.dp))
                        Text("Phone: ${phone ?: "—"}", color = Slate200, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text(message, color = Slate400, style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        WhatsAppShare.send(context, phone, message)
                        vm.dismissSharePrompt()
                    }) { Text("Send via WhatsApp", color = BrandCyan, fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        s.pdfUrl?.let {
                            val i = android.content.Intent(android.content.Intent.ACTION_VIEW, it.toUri())
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(i)
                        }
                        vm.dismissSharePrompt()
                    }) { Text("Open PDF", color = Slate200) }
                },
            )
        }
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Bill", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) }
                    },
                    actions = {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, null, tint = Danger) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().systemBarsPadding().padding(padding)) {
                when {
                    s.loading && s.bill == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandCyan)
                    }
                    s.bill != null -> BillBody(s.bill!!, s, vm, onShareClick = { vm.generatePdf() })
                    s.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun buildMessage(b: Bill, pdfUrl: String?): String {
    val sb = StringBuilder()
    sb.append("Hello ${b.tenantName.orEmpty()},\n")
    sb.append("Your bill for ${b.category.replaceFirstChar { it.uppercase() }} - ${b.billingMonth.take(7)}.\n")
    sb.append("Amount: ₹${"%.0f".format(b.amount)}\n")
    if (b.pending > 0) sb.append("Pending: ₹${"%.0f".format(b.pending)}\n")
    sb.append("Due date: ${b.dueDate}\n")
    pdfUrl?.let { sb.append("\nInvoice: $it") }
    return sb.toString()
}

@Composable
private fun BillBody(b: Bill, s: BillDetailUi, vm: BillDetailViewModel, onShareClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(20.dp)) {
            Text("${b.category.replaceFirstChar { it.uppercase() }}  ·  ${b.billingMonth.take(7)}", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(b.tenantName ?: "—", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                buildString {
                    append("User ID: ${b.tenantUserCode.orEmpty()}")
                    if (!b.roomNumber.isNullOrBlank()) append("  ·  Room ${b.roomNumber}")
                },
                color = Slate400, style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AmountBox(Modifier.weight(1f), "Amount", b.amount, BrandCyan)
                AmountBox(Modifier.weight(1f), "Paid", b.amountPaid, Success)
                AmountBox(Modifier.weight(1f), "Pending", b.pending, if (b.pending > 0) Warning else Slate400)
            }
            if (!b.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(b.description, color = Slate200, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Status", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Status", color = Slate400, modifier = Modifier.weight(1f))
                StatusChip(b.status)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text("Due date", color = Slate400, modifier = Modifier.weight(1f))
                Text(b.dueDate, color = Slate200)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (s.payments.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Payments", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                s.payments.forEach { p -> PaymentRow(p) }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (b.status != "paid") {
            PrimaryButton(text = "Record payment", onClick = { vm.openRecord(true) }, loading = s.recording)
            Spacer(Modifier.height(10.dp))
        }
        OutlinedAction(text = if (s.generatingPdf) "Generating PDF…" else "Generate & share PDF", icon = Icons.Outlined.PictureAsPdf, onClick = onShareClick)
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun AmountBox(modifier: Modifier, label: String, value: Double, accent: Color) {
    Column(modifier = modifier) {
        Text(label, color = Slate400, style = MaterialTheme.typography.bodySmall)
        Text("₹ %.0f".format(value), color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PaymentRow(p: Payment) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(p.paidAt.take(10), color = Slate400, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(p.method.replaceFirstChar { it.uppercase() }, color = Slate200, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text("₹ %.0f".format(p.amount), color = Success, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
    if (!p.reference.isNullOrBlank()) {
        Text("Ref: ${p.reference}", color = Slate400, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "paid" -> "Paid" to Success
        "partial" -> "Partial" to Warning
        "overdue" -> "Overdue" to Danger
        else -> "Unpaid" to Slate400
    }
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlinedAction(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = BrandCyan)
            Spacer(Modifier.height(0.dp))
            Text("  $text", color = BrandCyan, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onClick) { Text("Go", color = BrandCyan) }
        }
    }
}

@Composable
private fun RecordPaymentDialog(bill: Bill, submitting: Boolean, error: String?, onDismiss: () -> Unit, onSubmit: (amount: Double, method: String, reference: String?) -> Unit) {
    var amount by remember { mutableStateOf(bill.pending.toInt().toString()) }
    var method by remember { mutableStateOf("cash") }
    var reference by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record payment", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("${bill.tenantName.orEmpty()} · ${bill.category} · pending ₹%.0f".format(bill.pending), color = Slate200, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("cash", "upi", "bank_transfer", "card", "other").forEach { m ->
                        TextButton(onClick = { method = m }) { Text(m, color = if (m == method) BrandCyan else Slate400, style = MaterialTheme.typography.bodySmall) }
                    }
                }
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Reference (UPI ID, txn no.)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                if (error != null) Text(error, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(enabled = !submitting && (amount.toDoubleOrNull() ?: 0.0) > 0.0, onClick = {
                onSubmit(amount.toDouble(), method, reference.trim().ifBlank { null })
            }) { Text(if (submitting) "Saving…" else "Save", color = BrandCyan) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Slate200) } },
    )
}
