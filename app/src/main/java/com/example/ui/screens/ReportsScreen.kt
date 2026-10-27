package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ReservationStatus
import com.example.data.model.RoomStatus
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.executiveMetrics.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val config by viewModel.config.collectAsState()

    val currency = config?.currencySymbol ?: "$"
    val totalRevenue = payments.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val profit = totalRevenue - totalExpenses
    val profitMargin = if (totalRevenue > 0) ((profit / totalRevenue) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Executive Performance Report",
                    fontFamily = FontFamily.Serif,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
                Text(
                    text = "Consolidated luxury hospitality analytics & financial reconciliation",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
            }
        }

        // Top Financial Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraMidnightNavy),
                modifier = Modifier.fillMaxWidth().testTag("report_summary_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("OPERATING PROFIT MARGIN", fontSize = 10.sp, color = LuxoraChampagneGold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$profitMargin%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraPureWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Net Operating Income: $currency${String.format(Locale.US, "%,.2f", profit)}",
                        color = LuxoraChampagneLight,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = LuxoraDarkBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Revenue", fontSize = 11.sp, color = LuxoraSlateGray)
                            Text("$currency${String.format(Locale.US, "%,.0f", totalRevenue)}", fontWeight = FontWeight.Bold, color = LuxoraPureWhite)
                        }
                        Column {
                            Text("Operating Expenses", fontSize = 11.sp, color = LuxoraSlateGray)
                            Text("$currency${String.format(Locale.US, "%,.0f", totalExpenses)}", fontWeight = FontWeight.Bold, color = LuxoraWarningAmber)
                        }
                        Column {
                            Text("RevPAR", fontSize = 11.sp, color = LuxoraSlateGray)
                            Text("$currency${String.format(Locale.US, "%.0f", metrics.revPar)}", fontWeight = FontWeight.Bold, color = LuxoraChampagneGold)
                        }
                    }
                }
            }
        }

        // Room Occupancy & ADR Section
        item {
            SectionHeader(title = "Occupancy & Room Metrics")
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
                border = BorderStroke(1.dp, LuxoraBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportRow(label = "Total Room Inventory:", value = "${metrics.totalRooms} Suites")
                    ReportRow(label = "Currently Occupied:", value = "${metrics.occupiedRooms} Suites (${metrics.occupancyPercent}%)")
                    ReportRow(label = "Available for Sale:", value = "${metrics.availableRooms} Suites")
                    ReportRow(label = "Under Turnover / Cleaning:", value = "${metrics.cleaningRooms} Suites")
                    ReportRow(label = "Under Engineering Maintenance:", value = "${metrics.maintenanceRooms} Suites")
                    HorizontalDivider(color = LuxoraBorderLight)
                    ReportRow(label = "Average Daily Rate (ADR):", value = "$currency${String.format(Locale.US, "%.2f", metrics.adr)}", isBold = true)
                    ReportRow(label = "Revenue Per Available Room (RevPAR):", value = "$currency${String.format(Locale.US, "%.2f", metrics.revPar)}", isBold = true)
                }
            }
        }

        // Reservation Health
        item {
            SectionHeader(title = "Booking Portfolio Activity")
            val confirmed = reservations.count { it.reservation.status == ReservationStatus.CONFIRMED }
            val inHouse = reservations.count { it.reservation.status == ReservationStatus.CHECKED_IN }
            val completed = reservations.count { it.reservation.status == ReservationStatus.CHECKED_OUT }
            val cancelled = reservations.count { it.reservation.status == ReservationStatus.CANCELLED }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
                border = BorderStroke(1.dp, LuxoraBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportRow(label = "Total Bookings Recorded:", value = "${reservations.size}")
                    ReportRow(label = "Active In-House Stays:", value = "$inHouse")
                    ReportRow(label = "Upcoming Confirmed Bookings:", value = "$confirmed")
                    ReportRow(label = "Completed Check-Outs:", value = "$completed")
                    ReportRow(label = "Cancellations:", value = "$cancelled (${if (reservations.isNotEmpty()) (cancelled * 100 / reservations.size) else 0}%)")
                    HorizontalDivider(color = LuxoraBorderLight)
                    ReportRow(
                        label = "Outstanding Guest Folio Balances:",
                        value = "$currency${String.format(Locale.US, "%,.2f", metrics.outstandingBalance)}",
                        isBold = true,
                        valueColor = LuxoraWarningAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = LuxoraMidnightNavy
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isBold) LuxoraMidnightNavy else LuxoraSlateGray,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
