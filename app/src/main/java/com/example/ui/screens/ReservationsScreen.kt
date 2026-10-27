package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun ReservationsScreen(
    viewModel: LuxoraViewModel,
    onNavigateToFolio: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val reservations by viewModel.reservations.collectAsState()
    val guests by viewModel.guests.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val roomTypes by viewModel.roomTypes.collectAsState()
    val config by viewModel.config.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<ReservationStatus?>(null) }
    var showNewReservationDialog by remember { mutableStateOf(false) }

    val filteredReservations = remember(reservations, searchQuery, selectedFilter) {
        reservations.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.reservation.bookingReference.contains(searchQuery.trim(), ignoreCase = true) ||
                    (item.guest?.fullName?.contains(searchQuery.trim(), ignoreCase = true) == true) ||
                    (item.room?.roomNumber?.contains(searchQuery.trim(), ignoreCase = true) == true)

            val matchStatus = selectedFilter == null || item.reservation.status == selectedFilter
            matchQuery && matchStatus
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewReservationDialog = true },
                containerColor = LuxoraDeepEmerald,
                contentColor = LuxoraPureWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_reservation")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Booking")
            }
        },
        containerColor = LuxoraWarmIvory,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by booking ref, guest name, or room...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuxoraSlateGray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuxoraPureWhite,
                    unfocusedContainerColor = LuxoraPureWhite,
                    focusedBorderColor = LuxoraDeepEmerald,
                    unfocusedBorderColor = LuxoraBorderLight
                ),
                modifier = Modifier.fillMaxWidth().testTag("input_search_reservations")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${reservations.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxoraMidnightNavy,
                            selectedLabelColor = LuxoraPureWhite
                        )
                    )
                }
                items(ReservationStatus.values()) { status ->
                    val count = reservations.count { it.reservation.status == status }
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = {
                            selectedFilter = if (selectedFilter == status) null else status
                        },
                        label = { Text("${status.label} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxoraMidnightNavy,
                            selectedLabelColor = LuxoraPureWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reservations List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredReservations, key = { it.reservation.id }) { item ->
                    ReservationCard(
                        item = item,
                        currencySymbol = config?.currencySymbol ?: "$",
                        onCheckIn = { viewModel.checkInGuest(item.reservation.id) },
                        onCheckOut = { viewModel.checkOutGuest(item.reservation.id) },
                        onViewFolio = { onNavigateToFolio(item.reservation.id) },
                        onCancel = { viewModel.cancelReservation(item.reservation.id) }
                    )
                }
            }
        }
    }

    // New Reservation Wizard Dialog
    if (showNewReservationDialog) {
        NewReservationDialog(
            guests = guests,
            rooms = rooms,
            roomTypes = roomTypes,
            currencySymbol = config?.currencySymbol ?: "$",
            taxRate = config?.taxRatePercent ?: 12.0,
            onDismiss = { showNewReservationDialog = false },
            onSave = { guestId, roomId, typeId, checkIn, checkOut, nights, adults, children, rate, notes ->
                viewModel.createReservation(
                    guestId = guestId,
                    roomId = roomId,
                    roomTypeId = typeId,
                    checkInDate = checkIn,
                    checkOutDate = checkOut,
                    nights = nights,
                    adults = adults,
                    children = children,
                    nightlyRate = rate,
                    specialRequests = notes
                ) {
                    showNewReservationDialog = false
                }
            }
        )
    }
}

@Composable
private fun ReservationCard(
    item: ReservationWithDetails,
    currencySymbol: String,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onViewFolio: () -> Unit,
    onCancel: () -> Unit
) {
    val res = item.reservation
    val guest = item.guest
    val room = item.room
    val type = item.roomType

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("res_card_${res.bookingReference}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Reference & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = res.bookingReference,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraMidnightNavy
                    )
                    if (res.keyCardIssued) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Key, contentDescription = "Key Card Issued", modifier = Modifier.size(16.dp), tint = LuxoraDeepEmerald)
                    }
                }
                ReservationStatusBadge(res.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guest Name & VIP Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = guest?.fullName ?: "Guest",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
                if (guest?.isVip == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge("VIP", LuxoraChampagneLight, LuxoraMidnightNavy)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Room Info
            Text(
                text = "Room ${room?.roomNumber ?: "Pending"} • ${type?.name ?: "Suite"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = LuxoraDeepEmerald
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dates & Guests
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${res.checkInDate} → ${res.checkOutDate} (${res.nights}n)",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
                Text(
                    text = "${res.adults} Adults${if (res.children > 0) ", ${res.children} Child" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            // Financial summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: $currencySymbol${String.format(Locale.US, "%,.2f", res.totalAmount)}",
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

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = onViewFolio,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Folio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LuxoraDeepEmerald)
                    }

                    when (res.status) {
                        ReservationStatus.CONFIRMED -> {
                            Button(
                                onClick = onCheckIn,
                                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_card_checkin_${res.bookingReference}")
                            ) {
                                Text("Check In", fontSize = 11.sp)
                            }
                        }
                        ReservationStatus.CHECKED_IN -> {
                            Button(
                                onClick = onCheckOut,
                                colors = ButtonDefaults.buttonColors(containerColor = LuxoraMidnightNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_card_checkout_${res.bookingReference}")
                            ) {
                                Text("Check Out", fontSize = 11.sp, color = LuxoraChampagneGold)
                            }
                        }
                        else -> {}
                    }

                    if (res.status == ReservationStatus.CONFIRMED || res.status == ReservationStatus.PENDING) {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = BorderStroke(0.8.dp, LuxoraErrorBurgundy)
                        ) {
                            Text("Cancel", fontSize = 11.sp, color = LuxoraErrorBurgundy)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewReservationDialog(
    guests: List<GuestEntity>,
    rooms: List<RoomWithDetails>,
    roomTypes: List<RoomTypeEntity>,
    currencySymbol: String,
    taxRate: Double,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Long, String, String, Int, Int, Int, Double, String) -> Unit
) {
    var selectedGuestId by remember { mutableStateOf(guests.firstOrNull()?.id ?: 1L) }
    var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.room?.id ?: 1L) }
    var checkInDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var nightsStr by remember { mutableStateOf("3") }
    var adultsStr by remember { mutableStateOf("2") }
    var childrenStr by remember { mutableStateOf("0") }
    var specialRequests by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedRoomWithDetails = rooms.find { it.room.id == selectedRoomId }
    val roomType = selectedRoomWithDetails?.roomType ?: roomTypes.firstOrNull()
    val nightlyRate = roomType?.basePrice ?: 500.0

    val nights = nightsStr.toIntOrNull() ?: 1
    val subtotal = nightlyRate * nights
    val taxAmount = subtotal * (taxRate / 100.0)
    val totalEstimated = subtotal + taxAmount

    // Calculate checkout date
    val checkOutDate = remember(checkInDate, nights) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(checkInDate) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, nights)
            sdf.format(cal.time)
        } catch (e: Exception) {
            "2026-10-30"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Luxury Reservation",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = LuxoraMidnightNavy
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Select Guest", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    guests.forEach { g ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGuestId = g.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedGuestId == g.id,
                                onClick = { selectedGuestId = g.id },
                                colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${g.fullName} ${if (g.isVip) "★ VIP" else ""}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item {
                    Text("Select Room", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    rooms.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRoomId = r.room.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedRoomId == r.room.id,
                                onClick = { selectedRoomId = r.room.id },
                                colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Room ${r.room.roomNumber} (${r.roomType?.name ?: "Suite"}) • $currencySymbol${r.roomType?.basePrice?.toInt() ?: 0}/nt",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = { checkInDate = it },
                        label = { Text("Check-In Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_checkin_date")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nightsStr,
                            onValueChange = { nightsStr = it },
                            label = { Text("Nights") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_nights")
                        )
                        OutlinedTextField(
                            value = adultsStr,
                            onValueChange = { adultsStr = it },
                            label = { Text("Adults") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = specialRequests,
                        onValueChange = { specialRequests = it },
                        label = { Text("Special Requests / VIP Amenities") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Cost Summary Box
                item {
                    Surface(
                        color = LuxoraWarmIvory,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LuxoraBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Check-Out Date:", style = MaterialTheme.typography.bodySmall)
                                Text(checkOutDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal ($nights nights):", style = MaterialTheme.typography.bodySmall)
                                Text("$currencySymbol${String.format(Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Taxes & Fees ($taxRate%):", style = MaterialTheme.typography.bodySmall)
                                Text("$currencySymbol${String.format(Locale.US, "%.2f", taxAmount)}", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "$currencySymbol${String.format(Locale.US, "%.2f", totalEstimated)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxoraDeepEmerald
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(errorMessage!!, color = LuxoraErrorBurgundy, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val n = nightsStr.toIntOrNull() ?: 1
                    val a = adultsStr.toIntOrNull() ?: 1
                    val c = childrenStr.toIntOrNull() ?: 0
                    val typeId = selectedRoomWithDetails?.room?.roomTypeId ?: 1L
                    onSave(selectedGuestId, selectedRoomId, typeId, checkInDate.trim(), checkOutDate, n, a, c, nightlyRate, specialRequests)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_reservation")
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
