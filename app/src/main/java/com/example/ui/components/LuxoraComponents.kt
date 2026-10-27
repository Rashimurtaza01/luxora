package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun LuxoraMonogram(
    size: Int = 36,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LuxoraMidnightNavy),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "L",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.55).sp,
                color = LuxoraChampagneGold
            )
        }
    }
}

@Composable
fun StatusBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RoomStatusBadge(status: RoomStatus) {
    val (bg, fg) = when (status) {
        RoomStatus.AVAILABLE -> LuxoraSuccessContainer to LuxoraSuccessGreen
        RoomStatus.RESERVED -> LuxoraWarningContainer to LuxoraWarningAmber
        RoomStatus.OCCUPIED -> LuxoraMidnightNavy.copy(alpha = 0.12f) to LuxoraMidnightNavy
        RoomStatus.MAINTENANCE -> LuxoraErrorContainer to LuxoraErrorBurgundy
        RoomStatus.OUT_OF_ORDER -> LuxoraBorderLight to LuxoraSlateGray
    }
    StatusBadge(label = status.label, containerColor = bg, contentColor = fg)
}

@Composable
fun CleanlinessBadge(status: CleanlinessStatus) {
    val (bg, fg) = when (status) {
        CleanlinessStatus.READY -> LuxoraSuccessContainer to LuxoraSuccessGreen
        CleanlinessStatus.INSPECTED -> LuxoraInfoContainer to LuxoraInfoBlue
        CleanlinessStatus.CLEANING -> LuxoraWarningContainer to LuxoraWarningAmber
        CleanlinessStatus.DIRTY -> LuxoraErrorContainer to LuxoraErrorBurgundy
    }
    StatusBadge(label = status.label, containerColor = bg, contentColor = fg)
}

@Composable
fun ReservationStatusBadge(status: ReservationStatus) {
    val (bg, fg) = when (status) {
        ReservationStatus.CONFIRMED -> LuxoraDeepEmerald.copy(alpha = 0.15f) to LuxoraDeepEmerald
        ReservationStatus.CHECKED_IN -> LuxoraSuccessContainer to LuxoraSuccessGreen
        ReservationStatus.CHECKED_OUT -> LuxoraBorderLight to LuxoraSlateGray
        ReservationStatus.PENDING -> LuxoraWarningContainer to LuxoraWarningAmber
        ReservationStatus.CANCELLED -> LuxoraErrorContainer to LuxoraErrorBurgundy
    }
    StatusBadge(label = status.label, containerColor = bg, contentColor = fg)
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = LuxoraDeepEmerald,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = LuxoraSlateGray,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = LuxoraMidnightNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (subtitle != null || badgeText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (badgeText != null) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = LuxoraDeepEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxoraSlateGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LuxoraMidnightNavy
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = LuxoraDeepEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
