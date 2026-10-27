package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HotelConfigEntity
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var hotelName by remember(config) { mutableStateOf(config?.hotelName ?: "LUXORA Grand Palace & Spa") }
    var currencySymbol by remember(config) { mutableStateOf(config?.currencySymbol ?: "$") }
    var currencyCode by remember(config) { mutableStateOf(config?.currencyCode ?: "USD") }
    var taxRateStr by remember(config) { mutableStateOf((config?.taxRatePercent ?: 12.0).toString()) }
    var serviceChargeStr by remember(config) { mutableStateOf((config?.serviceChargePercent ?: 5.0).toString()) }
    var checkInTime by remember(config) { mutableStateOf(config?.checkInTime ?: "15:00") }
    var checkOutTime by remember(config) { mutableStateOf(config?.checkOutTime ?: "12:00") }

    var showResetDialog by remember { mutableStateOf(false) }

    val currencies = listOf(
        Triple("$", "USD", "US Dollar ($)"),
        Triple("Rs", "PKR", "Pakistani Rupee (Rs)"),
        Triple("€", "EUR", "Euro (€)"),
        Triple("£", "GBP", "British Pound (£)"),
        Triple("د.إ", "AED", "UAE Dirham (د.إ)")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Hotel Configuration & Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
                Text(
                    text = "Configure property branding, default currency, and operational rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
            }
        }

        // Active Role Switcher
        item {
            SectionHeader(title = "Fast Role Switcher (Demo & Testing)")
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
                border = BorderStroke(1.dp, LuxoraBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Switch between system roles to test permissions and workflows:", fontSize = 12.sp, color = LuxoraSlateGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    allUsers.forEach { user ->
                        val isSelected = currentUser?.id == user.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.switchUser(user) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.switchUser(user) },
                                colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${user.fullName} (${user.role.displayName})",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) LuxoraMidnightNavy else LuxoraSlateGray
                                )
                                Text("Username: ${user.username}", fontSize = 10.sp, color = LuxoraSlateGray)
                            }
                        }
                    }
                }
            }
        }

        // Hotel Identity
        item {
            SectionHeader(title = "Property Branding & Profile")
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
                border = BorderStroke(1.dp, LuxoraBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hotelName,
                        onValueChange = { hotelName = it },
                        label = { Text("Property Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_hotel_name")
                    )

                    Text("Operating Currency", style = MaterialTheme.typography.labelSmall)
                    currencies.forEach { (sym, code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currencySymbol = sym
                                    currencyCode = code
                                }
                        ) {
                            RadioButton(
                                selected = currencyCode == code,
                                onClick = {
                                    currencySymbol = sym
                                    currencyCode = code
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = taxRateStr,
                            onValueChange = { taxRateStr = it },
                            label = { Text("Tax Rate (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = serviceChargeStr,
                            onValueChange = { serviceChargeStr = it },
                            label = { Text("Service Charge (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = checkInTime,
                            onValueChange = { checkInTime = it },
                            label = { Text("Check-In Time") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = checkOutTime,
                            onValueChange = { checkOutTime = it },
                            label = { Text("Check-Out Time") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val tax = taxRateStr.toDoubleOrNull() ?: 12.0
                            val svc = serviceChargeStr.toDoubleOrNull() ?: 5.0
                            viewModel.updateConfig(
                                (config ?: HotelConfigEntity()).copy(
                                    hotelName = hotelName.trim(),
                                    currencySymbol = currencySymbol,
                                    currencyCode = currencyCode,
                                    taxRatePercent = tax,
                                    serviceChargePercent = svc,
                                    checkInTime = checkInTime.trim(),
                                    checkOutTime = checkOutTime.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_save_settings")
                    ) {
                        Text("Save Configuration")
                    }
                }
            }
        }

        // Reseed Demo Database
        item {
            SectionHeader(title = "Database Administration")
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
                border = BorderStroke(1.dp, LuxoraErrorBurgundy.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reset Demo Catalog", fontWeight = FontWeight.Bold, color = LuxoraErrorBurgundy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Restores all rooms, luxury penthouse suites, sample VIP guests, folio records, inventory, and staff rosters back to initial 5-star baseline.",
                        fontSize = 11.sp,
                        color = LuxoraSlateGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        border = BorderStroke(1.dp, LuxoraErrorBurgundy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_reset_demo_data")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = LuxoraErrorBurgundy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reseed Baseline Database", color = LuxoraErrorBurgundy, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reseed Database?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset all hotel tables and populate fresh 5-star sample suites, reservations, guests, and folio data.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDemoDatabase()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxoraErrorBurgundy)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
