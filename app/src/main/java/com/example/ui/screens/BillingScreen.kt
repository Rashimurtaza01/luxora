package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val payments by viewModel.payments.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val config by viewModel.config.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var selectedInvoiceForPrint by remember { mutableStateOf<InvoiceEntity?>(null) }

    val currency = config?.currencySymbol ?: "$"
    val totalRevenue = payments.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netOperatingProfit = totalRevenue - totalExpenses

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
    ) {
        // Financial KPI Header
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = LuxoraMidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL REVENUE", fontSize = 10.sp, color = LuxoraChampagneGold, letterSpacing = 1.sp)
                    Text("$currency${String.format(Locale.US, "%,.0f", totalRevenue)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LuxoraPureWhite)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXPENSES", fontSize = 10.sp, color = LuxoraChampagneLight, letterSpacing = 1.sp)
                    Text("$currency${String.format(Locale.US, "%,.0f", totalExpenses)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LuxoraWarningAmber)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NET PROFIT", fontSize = 10.sp, color = LuxoraChampagneGold, letterSpacing = 1.sp)
                    Text("$currency${String.format(Locale.US, "%,.0f", netOperatingProfit)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LuxoraSuccessGreen)
                }
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = LuxoraPureWhite,
            contentColor = LuxoraDeepEmerald,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = LuxoraDeepEmerald
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Invoices (${invoices.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Payments (${payments.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Expenses (${expenses.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // Invoices
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(invoices, key = { it.id }) { inv ->
                            InvoiceCard(
                                inv = inv,
                                currency = currency,
                                onPrint = { selectedInvoiceForPrint = inv }
                            )
                        }
                    }
                }
                1 -> {
                    // Payments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(payments, key = { it.id }) { p ->
                            PaymentCard(payment = p, currency = currency)
                        }
                    }
                }
                2 -> {
                    // Expenses
                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { showExpenseDialog = true },
                                containerColor = LuxoraDeepEmerald,
                                contentColor = LuxoraPureWhite,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("fab_add_expense")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Expense")
                            }
                        },
                        containerColor = LuxoraWarmIvory
                    ) { p ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(p)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(expenses, key = { it.id }) { exp ->
                                ExpenseCard(expense = exp, currency = currency)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            currencySymbol = currency,
            onDismiss = { showExpenseDialog = false },
            onConfirm = { cat, desc, amt, method, vendor ->
                viewModel.createExpense(cat, desc, amt, method, vendor)
                showExpenseDialog = false
            }
        )
    }

    selectedInvoiceForPrint?.let { inv ->
        PrintableInvoiceDialog(
            invoice = inv,
            currency = currency,
            onDismiss = { selectedInvoiceForPrint = null }
        )
    }
}

@Composable
private fun InvoiceCard(
    inv: InvoiceEntity,
    currency: String,
    onPrint: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("invoice_card_${inv.invoiceNumber}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                    Text("Date: ${inv.issueDate}", fontSize = 11.sp, color = LuxoraSlateGray)
                }
                if (inv.isPaid) {
                    StatusBadge("PAID IN FULL", LuxoraSuccessContainer, LuxoraSuccessGreen)
                } else {
                    StatusBadge("UNPAID BALANCE", LuxoraErrorContainer, LuxoraErrorBurgundy)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Guest: ${inv.guestName} • Room ${inv.roomNumber}", style = MaterialTheme.typography.bodyMedium, color = LuxoraDeepEmerald)

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal: $currency${String.format(Locale.US, "%.2f", inv.subtotal)}", fontSize = 11.sp, color = LuxoraSlateGray)
                Text("Tax: $currency${String.format(Locale.US, "%.2f", inv.taxAmount)}", fontSize = 11.sp, color = LuxoraSlateGray)
                Text("Grand Total: $currency${String.format(Locale.US, "%.2f", inv.grandTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Balance: $currency${String.format(Locale.US, "%.2f", inv.balanceDue)}",
                    fontWeight = FontWeight.Bold,
                    color = if (inv.balanceDue > 0.01) LuxoraErrorBurgundy else LuxoraSuccessGreen
                )
                TextButton(onClick = onPrint) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = LuxoraDeepEmerald)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print / Voucher", fontSize = 11.sp, color = LuxoraDeepEmerald)
                }
            }
        }
    }
}

@Composable
private fun PaymentCard(
    payment: PaymentEntity,
    currency: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(payment.transactionRef, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LuxoraMidnightNavy)
                Text("Method: ${payment.method.label} • ${payment.dateString}", fontSize = 11.sp, color = LuxoraSlateGray)
                if (payment.notes.isNotBlank()) {
                    Text("Note: ${payment.notes}", fontSize = 10.sp, color = LuxoraSlateGray)
                }
            }
            Text(
                "+$currency${String.format(Locale.US, "%,.2f", payment.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = LuxoraSuccessGreen
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseEntity,
    currency: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(expense.description, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LuxoraMidnightNavy)
                Text("${expense.category} • Vendor: ${expense.vendor}", fontSize = 11.sp, color = LuxoraSlateGray)
                Text("Approved By: ${expense.approvedBy} • ${expense.dateString}", fontSize = 10.sp, color = LuxoraSlateGray)
            }
            Text(
                "-$currency${String.format(Locale.US, "%,.2f", expense.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = LuxoraErrorBurgundy
            )
        }
    }
}

@Composable
private fun AddExpenseDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, PaymentMethod, String) -> Unit
) {
    var category by remember { mutableStateOf("F&B Procurement") }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }

    val categories = listOf("F&B Procurement", "Housekeeping Supplies", "Utilities & Power", "Maintenance & Repairs", "Marketing", "Administration")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Hotel Operating Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Expense Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_desc")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_amount")
                )

                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Vendor / Supplier Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && description.isNotBlank()) {
                        onConfirm(category, description.trim(), amt, method, vendor.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_expense")
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PrintableInvoiceDialog(
    invoice: InvoiceEntity,
    currency: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("LUXORA OFFICIAL VOUCHER", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                Text(invoice.invoiceNumber, fontSize = 12.sp, color = LuxoraSlateGray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = LuxoraWarmIvory, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Bill To: ${invoice.guestName}", fontWeight = FontWeight.Bold)
                        Text("Room Assigned: ${invoice.roomNumber}", fontSize = 12.sp)
                        Text("Issue Date: ${invoice.issueDate}", fontSize = 11.sp, color = LuxoraSlateGray)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:")
                    Text("$currency${String.format(Locale.US, "%.2f", invoice.subtotal)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Taxes & State Fees:")
                    Text("$currency${String.format(Locale.US, "%.2f", invoice.taxAmount)}")
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total:", fontWeight = FontWeight.Bold)
                    Text("$currency${String.format(Locale.US, "%.2f", invoice.grandTotal)}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payments Credited:")
                    Text("-$currency${String.format(Locale.US, "%.2f", invoice.paidAmount)}", color = LuxoraSuccessGreen)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Balance Due:", fontWeight = FontWeight.Bold)
                    Text(
                        "$currency${String.format(Locale.US, "%.2f", invoice.balanceDue)}",
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.balanceDue > 0.01) LuxoraErrorBurgundy else LuxoraSuccessGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald)
            ) {
                Text("Export Voucher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
