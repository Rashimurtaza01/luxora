package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExecutiveMetrics
import com.example.ui.viewmodel.LuxoraScreen
import com.example.ui.viewmodel.LuxoraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: LuxoraViewModel,
    onNavigate: (LuxoraScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.executiveMetrics.collectAsState()
    val config by viewModel.config.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayReadable = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(Date()) }

    val todayArrivals = remember(reservations, todayStr) {
        reservations.filter { it.reservation.checkInDate == todayStr && it.reservation.status == ReservationStatus.CONFIRMED }
    }
    val todayDepartures = remember(reservations, todayStr) {
        reservations.filter { it.reservation.checkOutDate == todayStr && it.reservation.status == ReservationStatus.CHECKED_IN }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Luxury Hero Greeting Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraMidnightNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LUXORA PALACE & SPA",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuxoraChampagneGold,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Staff"}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = LuxoraPureWhite,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuxoraDeepEmerald)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentUser?.role?.displayName ?: "Staff",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuxoraChampagneLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = todayReadable,
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxoraChampagneLight.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = LuxoraDarkBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Occupancy Progress Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Occupancy Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LuxoraWarmIvory
                        )
                        Text(
                            text = "${metrics.occupancyPercent}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = LuxoraChampagneGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (metrics.occupancyPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = LuxoraChampagneGold,
                        trackColor = LuxoraDarkCard
                    )
                }
            }
        }

        // Quick Operations Action Row
        item {
            SectionHeader(title = "Front Desk Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(LuxoraScreen.RESERVATIONS) },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("action_new_reservation")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Booking", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigate(LuxoraScreen.FRONT_DESK) },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxoraMidnightNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("action_front_desk")
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp), tint = LuxoraChampagneGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Front Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuxoraChampagneGold)
                }

                OutlinedButton(
                    onClick = { onNavigate(LuxoraScreen.HOUSEKEEPING) },
                    border = BorderStroke(1.dp, LuxoraDeepEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("action_housekeeping")
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp), tint = LuxoraDeepEmerald)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cleaning", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuxoraDeepEmerald)
                }
            }
        }

        // Key Executive Hospitality Metrics (2x2 Grid)
        item {
            SectionHeader(title = "Key Executive Metrics")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "RevPAR",
                        value = "${config?.currencySymbol}${String.format(Locale.US, "%.0f", metrics.revPar)}",
                        subtitle = "Per Available Room",
                        icon = Icons.Default.TrendingUp,
                        iconTint = LuxoraDeepEmerald,
                        modifier = Modifier.weight(1f).testTag("metric_revpar")
                    )
                    MetricStatCard(
                        title = "ADR",
                        value = "${config?.currencySymbol}${String.format(Locale.US, "%.0f", metrics.adr)}",
                        subtitle = "Avg Daily Room Rate",
                        icon = Icons.Default.Analytics,
                        iconTint = LuxoraChampagneDark,
                        modifier = Modifier.weight(1f).testTag("metric_adr")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Today's Revenue",
                        value = "${config?.currencySymbol}${String.format(Locale.US, "%,.0f", metrics.todayRevenue)}",
                        subtitle = "Month: ${config?.currencySymbol}${String.format(Locale.US, "%,.0f", metrics.monthlyRevenue)}",
                        icon = Icons.Default.Payments,
                        iconTint = LuxoraSuccessGreen,
                        modifier = Modifier.weight(1f).testTag("metric_revenue")
                    )
                    MetricStatCard(
                        title = "Guest Balances",
                        value = "${config?.currencySymbol}${String.format(Locale.US, "%,.0f", metrics.outstandingBalance)}",
                        subtitle = "Pending Settlement",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = LuxoraWarningAmber,
                        modifier = Modifier.weight(1f).testTag("metric_balance")
                    )
                }
            }
        }

        // Room Status Distribution
        item {
            SectionHeader(title = "Room Inventory & State", actionLabel = "View All Rooms", onActionClick = { onNavigate(LuxoraScreen.ROOMS) })
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
                border = BorderStroke(1.dp, LuxoraBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        RoomPillStat(label = "Total", count = metrics.totalRooms, color = LuxoraMidnightNavy)
                        RoomPillStat(label = "Occupied", count = metrics.occupiedRooms, color = LuxoraDeepEmerald)
                        RoomPillStat(label = "Available", count = metrics.availableRooms, color = LuxoraSuccessGreen)
                        RoomPillStat(label = "Dirty/Clean", count = metrics.cleaningRooms, color = LuxoraWarningAmber)
                        RoomPillStat(label = "Maint.", count = metrics.maintenanceRooms, color = LuxoraErrorBurgundy)
                    }
                }
            }
        }

        // Today's Arrivals
        item {
            SectionHeader(
                title = "Expected Arrivals (${todayArrivals.size})",
                actionLabel = "Front Desk",
                onActionClick = { onNavigate(LuxoraScreen.FRONT_DESK) }
            )
            if (todayArrivals.isEmpty()) {
                EmptyStateCard(message = "No pending arrivals scheduled for today.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayArrivals.forEach { res ->
                        ArrivalCard(
                            reservationWithDetails = res,
                            onCheckIn = { viewModel.checkInGuest(res.reservation.id) }
                        )
                    }
                }
            }
        }

        // Today's Departures
        item {
            SectionHeader(
                title = "Expected Departures (${todayDepartures.size})",
                actionLabel = "Front Desk",
                onActionClick = { onNavigate(LuxoraScreen.FRONT_DESK) }
            )
            if (todayDepartures.isEmpty()) {
                EmptyStateCard(message = "No departures pending for today.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayDepartures.forEach { res ->
                        DepartureCard(
                            reservationWithDetails = res,
                            onCheckOut = { viewModel.checkOutGuest(res.reservation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomPillStat(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LuxoraSlateGray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = LuxoraSlateGray
            )
        }
    }
}

@Composable
private fun ArrivalCard(
    reservationWithDetails: ReservationWithDetails,
    onCheckIn: () -> Unit
) {
    val res = reservationWithDetails.reservation
    val guest = reservationWithDetails.guest
    val room = reservationWithDetails.room

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("arrival_card_${res.bookingReference}")
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
                    Text(
                        text = guest?.fullName ?: "Guest",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraMidnightNavy
                    )
                    if (guest?.isVip == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge("VIP", LuxoraChampagneLight, LuxoraMidnightNavy)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Room ${room?.roomNumber ?: "Assigned"} • ${res.nights} nights • Ref: ${res.bookingReference}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
            }
            Button(
                onClick = onCheckIn,
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_checkin_${res.bookingReference}")
            ) {
                Text("Check In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DepartureCard(
    reservationWithDetails: ReservationWithDetails,
    onCheckOut: () -> Unit
) {
    val res = reservationWithDetails.reservation
    val guest = reservationWithDetails.guest
    val room = reservationWithDetails.room

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("departure_card_${res.bookingReference}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guest?.fullName ?: "Guest",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Room ${room?.roomNumber ?: ""} • Balance: $${String.format(Locale.US, "%.2f", res.balanceDue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (res.balanceDue > 0.01) LuxoraErrorBurgundy else LuxoraSuccessGreen
                )
            }
            Button(
                onClick = onCheckOut,
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraMidnightNavy),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_checkout_${res.bookingReference}")
            ) {
                Text("Check Out", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LuxoraChampagneGold)
            }
        }
    }
}
