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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
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
fun StaffAndInventoryScreen(
    viewModel: LuxoraViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val staffList by viewModel.staffList.collectAsState()
    val inventoryList by viewModel.inventoryList.collectAsState()
    val config by viewModel.config.collectAsState()

    val currency = config?.currencySymbol ?: "$"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = LuxoraMidnightNavy,
            contentColor = LuxoraChampagneGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = LuxoraChampagneGold
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Staff & Rosters (${staffList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Inventory Tracking (${inventoryList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (selectedTab == 0) {
            // Staff List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(staffList, key = { it.id }) { staff ->
                    StaffCard(
                        staff = staff,
                        onToggleDuty = {
                            val newSt = if (staff.status == StaffStatus.ON_DUTY) StaffStatus.OFF_DUTY else StaffStatus.ON_DUTY
                            viewModel.updateStaffStatus(staff, newSt)
                        }
                    )
                }
            }
        } else {
            // Inventory List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inventoryList, key = { it.id }) { item ->
                    InventoryCard(
                        item = item,
                        currency = currency,
                        onRestock = { qty -> viewModel.restockInventory(item.id, qty) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StaffCard(
    staff: StaffEntity,
    onToggleDuty: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("staff_card_${staff.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(staff.name, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("• ${staff.roleTitle}", fontSize = 12.sp, color = LuxoraDeepEmerald)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Dept: ${staff.department} • Shift: ${staff.shift.label} (${staff.shift.hours})",
                    fontSize = 11.sp,
                    color = LuxoraSlateGray
                )
                Text("Contact: ${staff.email} • ${staff.phone}", fontSize = 10.sp, color = LuxoraSlateGray)
            }

            Column(horizontalAlignment = Alignment.End) {
                if (staff.status == StaffStatus.ON_DUTY) {
                    StatusBadge("ON DUTY", LuxoraSuccessContainer, LuxoraSuccessGreen)
                } else {
                    StatusBadge("OFF DUTY", LuxoraBorderLight, LuxoraSlateGray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = onToggleDuty,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (staff.status == StaffStatus.ON_DUTY) "Set Off Duty" else "Set On Duty",
                        fontSize = 11.sp,
                        color = LuxoraDeepEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryCard(
    item: InventoryItemEntity,
    currency: String,
    onRestock: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("inventory_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                    Text("SKU: ${item.sku} • Cat: ${item.category}", fontSize = 11.sp, color = LuxoraSlateGray)
                }
                if (item.isLowStock) {
                    StatusBadge("LOW STOCK ALERT", LuxoraErrorContainer, LuxoraErrorBurgundy)
                } else {
                    StatusBadge("IN STOCK", LuxoraSuccessContainer, LuxoraSuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Current: ${item.currentStock} ${item.unit} (Min: ${item.minStockLevel})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isLowStock) LuxoraErrorBurgundy else LuxoraMidnightNavy
                )
                Text("Unit Cost: $currency${item.unitCost}", fontSize = 11.sp, color = LuxoraSlateGray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supplier: ${item.supplierName}", fontSize = 10.sp, color = LuxoraSlateGray)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { onRestock(10) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+10", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onRestock(25) },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+25 Restock", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
