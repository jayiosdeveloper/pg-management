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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.domain.model.Bill
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandPrimary
import com.pg.management.ui.theme.Danger
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400
import com.pg.management.ui.theme.Success
import com.pg.management.ui.theme.Warning
import java.time.YearMonth

@Composable
fun AdminBillingScreen(
    onBillClick: (String) -> Unit,
    vm: AdminBillingViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    if (s.showGenerateDialog) {
        AlertDialog(
            onDismissRequest = { vm.showGenerate(false) },
            title = { Text("Generate rent bills?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This creates a rent bill for every active member for ${s.month} using their monthly rent. Members who already have a bill for this month are skipped.", color = Slate200)
                    if (s.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(s.error!!, color = Danger, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !s.generating, onClick = { vm.generateRentForMonth() }) {
                    Text(if (s.generating) "Generating…" else "Generate", color = BrandCyan, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { vm.showGenerate(false) }) { Text("Cancel", color = Slate200) } },
        )
    }

    s.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::consumeMessage,
            confirmButton = { TextButton(onClick = vm::consumeMessage) { Text("OK", color = BrandCyan) } },
            title = { Text("Done", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.showGenerate(true) },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Generate ${s.month} rent", fontWeight = FontWeight.SemiBold) },
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
            Text("Bills", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(8.dp))
            MonthPicker(month = s.month, onChange = vm::setMonth)
            Spacer(Modifier.height(8.dp))
            SummaryRow(s.bills)
            Spacer(Modifier.height(12.dp))

            when {
                s.loading && s.bills.isEmpty() -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandCyan)
                }
                s.bills.isEmpty() -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No bills for ${s.month}.\nTap 'Generate ${s.month} rent' to issue rent bills for all members.",
                        color = Slate400,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.bills, key = { it.id }) { b -> BillRow(b, onClick = { onBillClick(b.id) }) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(month: String, onChange: (String) -> Unit) {
    val ym = remember(month) { runCatching { YearMonth.parse(month) }.getOrDefault(YearMonth.now()) }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(ym.minusMonths(1).toString()) }) { Icon(Icons.Outlined.ChevronLeft, null, tint = Color.White) }
        Box(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(month, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { onChange(ym.plusMonths(1).toString()) }) { Icon(Icons.Outlined.ChevronRight, null, tint = Color.White) }
    }
}

@Composable
private fun SummaryRow(bills: List<Bill>) {
    val total = bills.sumOf { it.amount }
    val paid = bills.sumOf { it.amountPaid }
    val pending = (total - paid).coerceAtLeast(0.0)
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StatMini(Modifier.weight(1f), "Bills", bills.size.toString(), Slate200)
            StatMini(Modifier.weight(1f), "Billed", "₹%.0f".format(total), BrandCyan)
            StatMini(Modifier.weight(1f), "Paid", "₹%.0f".format(paid), Success)
            StatMini(Modifier.weight(1f), "Pending", "₹%.0f".format(pending), if (pending > 0) Warning else Slate400)
        }
    }
}

@Composable
private fun StatMini(modifier: Modifier, label: String, value: String, accent: Color) {
    Column(modifier = modifier) {
        Text(label, color = Slate400, style = MaterialTheme.typography.bodySmall)
        Text(value, color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BillRow(b: Bill, onClick: () -> Unit) {
    val (label, color) = when (b.status) {
        "paid" -> "Paid" to Success
        "partial" -> "Partial" to Warning
        "overdue" -> "Overdue" to Danger
        else -> "Unpaid" to Slate400
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(b.tenantName ?: "—", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                "${b.category.replaceFirstChar { it.uppercase() }} · Room ${b.roomNumber.orEmpty()} · Due ${b.dueDate}",
                color = Slate400, style = MaterialTheme.typography.bodySmall,
            )
            if (b.pending > 0) Text("Pending: ₹%.0f".format(b.pending), color = Warning, style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("₹ %.0f".format(b.amount), color = Color.White, fontWeight = FontWeight.Bold)
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
