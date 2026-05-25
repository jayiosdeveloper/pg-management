package com.pg.management.ui.screens.admin.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.domain.model.Bill
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning

@Composable
fun AdminBillingScreen(vm: AdminBillingViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()

    if (s.showBulkDialog) BulkGenerateDialog(submitting = s.bulkSubmitting, error = s.error, onDismiss = { vm.showBulk(false) }, onSubmit = vm::bulkGenerate)
    s.recordingFor?.let { bill -> RecordPaymentDialog(bill, submitting = s.recording, error = s.error, onDismiss = { vm.openRecord(null) }, onSubmit = { amt, m, r -> vm.recordPayment(bill.id, amt, m, r) }) }
    s.message?.let { msg ->
        AlertDialog(onDismissRequest = vm::consumeMessage, confirmButton = { TextButton(onClick = vm::consumeMessage) { Text("OK", color = BrandCyan) } }, title = { Text("Done") }, text = { Text(msg) })
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.showBulk(true) },
                containerColor = BrandPrimary, contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Bulk generate", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(padding).padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Bills", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(12.dp))
            when {
                s.loading && s.bills.isEmpty() -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandCyan) }
                s.bills.isEmpty() -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text("No bills yet. Tap 'Bulk generate' to create rent bills for all active tenants.", color = Slate400, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.bills, key = { it.id }) { b -> BillRow(b, onRecordPayment = { vm.openRecord(b) }) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BillRow(b: Bill, onRecordPayment: () -> Unit) {
    val (label, color) = when (b.status) {
        "paid" -> "Paid" to Success
        "partial" -> "Partial" to Warning
        "overdue" -> "Overdue" to Danger
        else -> "Unpaid" to Slate400
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row {
            Column(Modifier.weight(1f)) {
                Text("${b.tenantName.orEmpty()} · ${b.category.replaceFirstChar { it.uppercase() }} · ${b.billingMonth.take(7)}", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Room ${b.roomNumber.orEmpty()} · Due ${b.dueDate}", color = Slate400, style = MaterialTheme.typography.bodySmall)
                if (b.pending > 0) Text("Pending: ₹ %.0f".format(b.pending), color = Warning, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹ %.0f".format(b.amount), color = Color.White, fontWeight = FontWeight.Bold)
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(label, color = color, style = MaterialTheme.typography.labelSmall)
                }
                if (b.status != "paid") {
                    TextButton(onClick = onRecordPayment, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("Record payment", color = BrandCyan, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun BulkGenerateDialog(submitting: Boolean, error: String?, onDismiss: () -> Unit, onSubmit: (category: String, month: String, amount: Double, dueDay: Int, description: String?) -> Unit) {
    var category by remember { mutableStateOf("rent") }
    var month by remember { mutableStateOf(java.time.LocalDate.now().toString().take(7)) }
    var amount by remember { mutableStateOf("5000") }
    var dueDay by remember { mutableStateOf("10") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk generate bills", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Creates a bill for every active tenant in the selected category & month.", color = Slate200, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                    OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("YYYY-MM") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Amount") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                    OutlinedTextField(value = dueDay, onValueChange = { dueDay = it.filter { ch -> ch.isDigit() } }, label = { Text("Due day") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = textFieldColors())
                if (error != null) Text(error, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(enabled = !submitting, onClick = {
                onSubmit(category.trim(), month.trim(), amount.toDoubleOrNull() ?: 0.0, dueDay.toIntOrNull() ?: 10, description.trim().ifBlank { null })
            }) { Text(if (submitting) "Generating…" else "Generate", color = BrandCyan) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Slate200) } },
    )
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
            }) { Text(if (submitting) "Recording…" else "Save", color = BrandCyan) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Slate200) } },
    )
}
