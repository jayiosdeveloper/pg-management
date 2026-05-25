package com.pg.management.ui.screens.admin.billing

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.pg.management.domain.model.MemberMonthStatus
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.screens.admin.tenants.textFieldColors
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning
import java.time.YearMonth

@Composable
fun AdminBillingScreen(
    onOpenElectricity: () -> Unit,
    vm: AdminBillingViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    s.partialFor?.let { row -> PartialAmountDialog(row, s.error, onDismiss = { vm.openPartial(null) }, onSubmit = { amt -> vm.mark(row, "partial", paidAmount = amt) }) }

    s.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::consumeMessage,
            confirmButton = { TextButton(onClick = vm::consumeMessage) { Text("OK", color = BrandCyan) } },
            title = { Text("Done") },
            text = { Text(msg) },
        )
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("Bills", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(8.dp))
            CategoryToggle(category = s.category, onChange = vm::setCategory, onOpenElectricity = onOpenElectricity)
            Spacer(Modifier.height(8.dp))
            MonthPicker(s.month, onChange = vm::setMonth)
            Spacer(Modifier.height(8.dp))
            SummaryBar(s.rows)
            Spacer(Modifier.height(12.dp))

            when {
                s.loading && s.rows.isEmpty() -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandCyan)
                }
                s.rows.isEmpty() -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text("No active members yet. Add members first.", color = Slate400)
                }
                s.error != null -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.rows, key = { it.tenantId }) { row -> MemberRow(row, busy = s.updatingTenantId == row.tenantId, vm = vm) }
                    item { Spacer(Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryToggle(category: String, onChange: (String) -> Unit, onOpenElectricity: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(4.dp),
        ) {
            listOf("rent" to "Rent", "electricity" to "Electricity").forEach { (key, label) ->
                val selected = category == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) BrandCyan.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable(onClick = { onChange(key) })
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, color = if (selected) BrandCyan else Slate200, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (category == "electricity") {
            Spacer(Modifier.height(0.dp))
            TextButton(onClick = onOpenElectricity) { Text("+ Reading", color = BrandCyan, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun MonthPicker(month: String, onChange: (String) -> Unit) {
    val ym = remember(month) { runCatching { YearMonth.parse(month) }.getOrDefault(YearMonth.now()) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(ym.minusMonths(1).toString()) }) { Icon(Icons.Outlined.ChevronLeft, null, tint = Color.White) }
        Box(
            Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(month, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { onChange(ym.plusMonths(1).toString()) }) { Icon(Icons.Outlined.ChevronRight, null, tint = Color.White) }
    }
}

@Composable
private fun SummaryBar(rows: List<MemberMonthStatus>) {
    val paid = rows.count { it.status == "paid" }
    val partial = rows.count { it.status == "partial" }
    val unpaid = rows.count { it.status == "unpaid" || it.status == "unbilled" || it.status == "overdue" }
    val collected = rows.sumOf { it.amountPaid }
    val expected = rows.sumOf { if (it.amount > 0) it.amount else it.monthlyRent }
    val pending = (expected - collected).coerceAtLeast(0.0)

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth()) {
            StatBox(Modifier.weight(1f), "Paid", paid.toString(), Success)
            StatBox(Modifier.weight(1f), "Partial", partial.toString(), Warning)
            StatBox(Modifier.weight(1f), "Unpaid", unpaid.toString(), Danger)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            StatBox(Modifier.weight(1f), "Collected", "₹%.0f".format(collected), Success)
            StatBox(Modifier.weight(1f), "Pending", "₹%.0f".format(pending), if (pending > 0) Warning else Slate400)
        }
    }
}

@Composable
private fun StatBox(modifier: Modifier, label: String, value: String, accent: Color) {
    Column(modifier = modifier) {
        Text(label, color = Slate400, style = MaterialTheme.typography.bodySmall)
        Text(value, color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MemberRow(row: MemberMonthStatus, busy: Boolean, vm: AdminBillingViewModel) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        AlertDialog(
            onDismissRequest = { showSheet = false },
            title = { Text(row.fullName, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose payment status for this month:", color = Slate200, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Rent: ₹%.0f".format(if (row.amount > 0) row.amount else row.monthlyRent), color = Slate400, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = { showSheet = false; vm.mark(row, "paid") }) { Text("Full payment done", color = Success, fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = { showSheet = false; vm.openPartial(row) }) { Text("Partial payment", color = Warning, fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = { showSheet = false; vm.mark(row, "unpaid") }) { Text("Mark as unpaid", color = Danger, fontWeight = FontWeight.SemiBold) }
                }
            },
            dismissButton = { TextButton(onClick = { showSheet = false }) { Text("Cancel", color = Slate200) } },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(enabled = !busy) { showSheet = true }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Person, null, tint = Slate400)
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(row.fullName, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                buildString {
                    append(row.userCode ?: "")
                    if (!row.roomNumber.isNullOrBlank()) append(" · Room ${row.roomNumber}")
                    if (!row.bedLabel.isNullOrBlank()) append(" · Bed ${row.bedLabel}")
                },
                color = Slate400, style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "₹%.0f / ₹%.0f".format(row.amountPaid, if (row.amount > 0) row.amount else row.monthlyRent),
                color = Slate200, style = MaterialTheme.typography.bodySmall,
            )
        }
        if (busy) CircularProgressIndicator(color = BrandCyan, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        else StatusBadge(row.status)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "paid" -> "Full Paid" to Success
        "partial" -> "Partial" to Warning
        "overdue" -> "Overdue" to Danger
        "unpaid" -> "Unpaid" to Danger
        else -> "Unbilled" to Slate400
    }
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PartialAmountDialog(row: MemberMonthStatus, error: String?, onDismiss: () -> Unit, onSubmit: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val total = if (row.amount > 0) row.amount else row.monthlyRent
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Partial payment", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("${row.fullName} · Total ₹%.0f. Enter how much they've paid (less than total).".format(total), color = Slate200, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount paid") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors(),
                )
                if (error != null) Text(error, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = { amount.toDoubleOrNull()?.let(onSubmit) },
            ) { Text("Save", color = BrandCyan) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Slate200) } },
    )
}
