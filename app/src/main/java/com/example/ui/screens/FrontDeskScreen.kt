package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrontDeskScreen(
    viewModel: LuxoraViewModel,
    initialFolioReservationId: Long? = null,
    modifier: Modifier = Modifier
) {
    val reservations by viewModel.reservations.collectAsState()
    val config by viewModel.config.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var folioResId by remember { mutableStateOf(initialFolioReservationId) }
    var showAddChargeDialog by remember { mutableStateOf<Long?>(null) }
    var showPaymentDialog by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(initialFolioReservationId) {
        if (initialFolioReservationId != null) {
            folioResId = initialFolioReservationId
        }
    }

    val arrivals = remember(reservations, todayStr) {
        reservations.filter { it.reservation.checkInDate <= todayStr && it.reservation.status == ReservationStatus.CONFIRMED }
    }
    val inHouse = remember(reservations) {
        reservations.filter { it.reservation.status == ReservationStatus.CHECKED_IN }
    }
    val departures = remember(reservations, todayStr) {
        reservations.filter { it.reservation.checkOutDate <= todayStr && it.reservation.status == ReservationStatus.CHECKED_IN }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
    ) {
        // Tab Navigation
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
                text = { Text("Arrivals (${arrivals.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("In-House (${inHouse.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Departures (${departures.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Arrivals
                    if (arrivals.isEmpty()) {
                        item {
                            EmptyFrontDeskState("No pending guest arrivals for today.")
                        }
                    } else {
                        items(arrivals, key = { it.reservation.id }) { item ->
                            FrontDeskArrivalItem(
                                item = item,
                                currencySymbol = config?.currencySymbol ?: "$",
                                onCheckIn = { viewModel.checkInGuest(item.reservation.id) },
                                onViewFolio = { folioResId = item.reservation.id }
                            )
                        }
                    }
                }
                1 -> {
                    // In-House
                    if (inHouse.isEmpty()) {
                        item {
                            EmptyFrontDeskState("No in-house guests currently registered.")
                        }
                    } else {
                        items(inHouse, key = { it.reservation.id }) { item ->
                            FrontDeskInHouseItem(
                                item = item,
                                currencySymbol = config?.currencySymbol ?: "$",
                                onAddCharge = { showAddChargeDialog = item.reservation.id },
                                onSettlePayment = { showPaymentDialog = item.reservation.id },
                                onCheckOut = { viewModel.checkOutGuest(item.reservation.id) },
                                onViewFolio = { folioResId = item.reservation.id }
                            )
                        }
                    }
                }
                2 -> {
                    // Departures
                    if (departures.isEmpty()) {
                        item {
                            EmptyFrontDeskState("No pending guest departures for today.")
                        }
                    } else {
                        items(departures, key = { it.reservation.id }) { item ->
                            FrontDeskDepartureItem(
                                item = item,
                                currencySymbol = config?.currencySymbol ?: "$",
                                onSettlePayment = { showPaymentDialog = item.reservation.id },
                                onCheckOut = { viewModel.checkOutGuest(item.reservation.id) },
                                onViewFolio = { folioResId = item.reservation.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Guest Folio / Invoice Dialog
    folioResId?.let { resId ->
        val selectedResWithDetails = reservations.find { it.reservation.id == resId }
        if (selectedResWithDetails != null) {
            GuestFolioDialog(
                viewModel = viewModel,
                item = selectedResWithDetails,
                currencySymbol = config?.currencySymbol ?: "$",
                onDismiss = { folioResId = null },
                onAddChargeClick = { showAddChargeDialog = resId },
                onPaymentClick = { showPaymentDialog = resId }
            )
        }
    }

    // Add Folio Charge Dialog
    showAddChargeDialog?.let { resId ->
        AddFolioChargeDialog(
            currencySymbol = config?.currencySymbol ?: "$",
            onDismiss = { showAddChargeDialog = null },
            onAdd = { category, description, amount, qty ->
                viewModel.addFolioCharge(resId, category, description, amount, qty)
                showAddChargeDialog = null
            }
        )
    }

    // Settle Payment Dialog
    showPaymentDialog?.let { resId ->
        val res = reservations.find { it.reservation.id == resId }
        val balance = res?.reservation?.balanceDue ?: 0.0
        RecordPaymentDialog(
            defaultAmount = balance,
            currencySymbol = config?.currencySymbol ?: "$",
            onDismiss = { showPaymentDialog = null },
            onConfirm = { amount, method, notes ->
                viewModel.recordPayment(resId, amount, method, notes) {
                    showPaymentDialog = null
                }
            }
        )
    }
}

@Composable
private fun FrontDeskArrivalItem(
    item: ReservationWithDetails,
    currencySymbol: String,
    onCheckIn: () -> Unit,
    onViewFolio: () -> Unit
) {
    val res = item.reservation
    val guest = item.guest
    val room = item.room

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("front_desk_arrival_${res.bookingReference}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = guest?.fullName ?: "Guest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraMidnightNavy
                    )
                    if (guest?.isVip == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge("VIP", LuxoraChampagneLight, LuxoraMidnightNavy)
                    }
                }
                Text(
                    text = "Ref: ${res.bookingReference}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Room ${room?.roomNumber} • ${item.roomType?.name} • ${res.nights} Nights",
                style = MaterialTheme.typography.bodyMedium,
                color = LuxoraDeepEmerald,
                fontWeight = FontWeight.Medium
            )

            if (res.specialRequests.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${res.specialRequests}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraWarningAmber,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deposit Paid: $currencySymbol${String.format(Locale.US, "%.2f", res.depositPaid)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onViewFolio) {
                        Text("View Folio", fontSize = 11.sp, color = LuxoraDeepEmerald)
                    }
                    Button(
                        onClick = onCheckIn,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_fd_checkin_${res.bookingReference}")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check In & Issue Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FrontDeskInHouseItem(
    item: ReservationWithDetails,
    currencySymbol: String,
    onAddCharge: () -> Unit,
    onSettlePayment: () -> Unit,
    onCheckOut: () -> Unit,
    onViewFolio: () -> Unit
) {
    val res = item.reservation
    val guest = item.guest
    val room = item.room

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("inhouse_${res.bookingReference}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LuxoraMidnightNavy)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ROOM ${room?.roomNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LuxoraChampagneGold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = guest?.fullName ?: "Guest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraMidnightNavy
                    )
                    if (guest?.isVip == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("★", color = LuxoraChampagneDark)
                    }
                }

                StatusBadge("In-House", LuxoraSuccessContainer, LuxoraSuccessGreen)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Departs: ${res.checkOutDate} • Stay: ${res.nights} Nights",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
                Text(
                    text = "Balance: $currencySymbol${String.format(Locale.US, "%,.2f", res.balanceDue)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (res.balanceDue > 0.01) LuxoraErrorBurgundy else LuxoraSuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewFolio,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Folio / Receipt", fontSize = 11.sp, color = LuxoraDeepEmerald, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onAddCharge,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(0.8.dp, LuxoraDeepEmerald)
                    ) {
                        Text("+ Charge", fontSize = 11.sp, color = LuxoraDeepEmerald)
                    }

                    if (res.balanceDue > 0.01) {
                        Button(
                            onClick = onSettlePayment,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxoraChampagneDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Settle", fontSize = 11.sp, color = LuxoraPureWhite)
                        }
                    }

                    Button(
                        onClick = onCheckOut,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxoraMidnightNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Check Out", fontSize = 11.sp, color = LuxoraChampagneGold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FrontDeskDepartureItem(
    item: ReservationWithDetails,
    currencySymbol: String,
    onSettlePayment: () -> Unit,
    onCheckOut: () -> Unit,
    onViewFolio: () -> Unit
) {
    val res = item.reservation
    val guest = item.guest
    val room = item.room

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("departure_${res.bookingReference}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room ${room?.roomNumber} • ${guest?.fullName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
                StatusBadge("Departure", LuxoraWarningContainer, LuxoraWarningAmber)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total Charges: $currencySymbol${String.format(Locale.US, "%,.2f", res.totalAmount)} • Paid: $currencySymbol${String.format(Locale.US, "%,.2f", res.depositPaid)}",
                style = MaterialTheme.typography.bodySmall,
                color = LuxoraSlateGray
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Balance: $currencySymbol${String.format(Locale.US, "%,.2f", res.balanceDue)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (res.balanceDue > 0.01) LuxoraErrorBurgundy else LuxoraSuccessGreen
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onViewFolio) {
                        Text("Folio", fontSize = 11.sp, color = LuxoraDeepEmerald)
                    }
                    if (res.balanceDue > 0.01) {
                        Button(
                            onClick = onSettlePayment,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxoraWarningAmber),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Settle Balance", fontSize = 11.sp, color = LuxoraPureWhite)
                        }
                    } else {
                        Button(
                            onClick = onCheckOut,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxoraMidnightNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_express_checkout_${res.bookingReference}")
                        ) {
                            Text("Complete Checkout", fontSize = 11.sp, color = LuxoraChampagneGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuestFolioDialog(
    viewModel: LuxoraViewModel,
    item: ReservationWithDetails,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAddChargeClick: () -> Unit,
    onPaymentClick: () -> Unit
) {
    val res = item.reservation
    val guest = item.guest
    val room = item.room
    val type = item.roomType

    LaunchedEffect(res.id) {
        viewModel.selectReservationForFolio(res.id)
    }

    val folioItems by viewModel.selectedFolioItems.collectAsState()
    val payments by viewModel.selectedPayments.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LUXORA GUEST FOLIO",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LuxoraMidnightNavy
                    )
                    Text(
                        text = "Ref: ${res.bookingReference} • Room ${room?.roomNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxoraSlateGray
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LuxoraDeepEmerald)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("OFFICIAL INVOICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LuxoraChampagneLight)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Guest Info Box
                    Surface(
                        color = LuxoraWarmIvory,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Guest: ${guest?.fullName ?: "N/A"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Email: ${guest?.email ?: "N/A"} • Country: ${guest?.country ?: "N/A"}", fontSize = 11.sp, color = LuxoraSlateGray)
                            Text("Dates: ${res.checkInDate} to ${res.checkOutDate} (${res.nights} nights)", fontSize = 11.sp, color = LuxoraSlateGray)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Itemized Room & Service Charges", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                }

                items(folioItems) { f ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(f.description, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("${f.category} • ${f.dateString}", fontSize = 10.sp, color = LuxoraSlateGray)
                        }
                        Text("$currencySymbol${String.format(Locale.US, "%.2f", f.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = LuxoraBorderLight.copy(alpha = 0.5f), thickness = 0.5.dp)
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Payments & Settlements", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                }

                if (payments.isEmpty()) {
                    item {
                        Text("No recorded payments yet.", fontSize = 11.sp, color = LuxoraSlateGray)
                    }
                } else {
                    items(payments) { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${p.method.label} (${p.transactionRef})", fontSize = 11.sp)
                                Text(p.dateString, fontSize = 9.sp, color = LuxoraSlateGray)
                            }
                            Text("-$currencySymbol${String.format(Locale.US, "%.2f", p.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LuxoraSuccessGreen)
                        }
                    }
                }

                // Balance summary
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = LuxoraMidnightNavy,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Folio Charges:", color = LuxoraChampagneLight, fontSize = 12.sp)
                                Text("$currencySymbol${String.format(Locale.US, "%,.2f", res.totalAmount)}", color = LuxoraChampagneLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payments Credited:", color = LuxoraChampagneLight, fontSize = 12.sp)
                                Text("$currencySymbol${String.format(Locale.US, "%,.2f", res.depositPaid)}", color = LuxoraChampagneLight, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = LuxoraDarkBorder, modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Outstanding Balance:", color = LuxoraPureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "$currencySymbol${String.format(Locale.US, "%,.2f", res.balanceDue)}",
                                    color = if (res.balanceDue > 0.01) LuxoraWarningAmber else LuxoraSuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddChargeClick) {
                    Text("+ Add Charge", fontSize = 11.sp)
                }
                Button(
                    onClick = onPaymentClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald)
                ) {
                    Text("Record Payment", fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AddFolioChargeDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Int) -> Unit
) {
    val categories = listOf("Fine Dining", "Spa & Wellness", "Minibar", "Laundry", "Airport Transfer", "Room Service", "Miscellaneous")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room Service / Folio Charge", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", style = MaterialTheme.typography.labelSmall)
                // Category buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(3).take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Item Description (e.g. Vintage Champagne)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_charge_desc")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Price ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_charge_amount")
                    )
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Qty") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val qty = qtyStr.toIntOrNull() ?: 1
                    if (amt > 0 && description.isNotBlank()) {
                        onAdd(selectedCategory, description.trim(), amt, qty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_add_charge")
            ) {
                Text("Add to Folio")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RecordPaymentDialog(
    defaultAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, PaymentMethod, String) -> Unit
) {
    var amountStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", defaultAmount)) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Folio Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Settlement Amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_amount")
                )

                Text("Payment Method", style = MaterialTheme.typography.labelSmall)
                PaymentMethod.values().forEach { method ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method.label, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reference / Card Last 4 / Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, selectedMethod, notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_payment")
            ) {
                Text("Record & Settle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmptyFrontDeskState(text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = LuxoraSlateGray)
        }
    }
}
