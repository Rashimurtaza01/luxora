package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxoraWarmIvory)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Notification Center",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
                Text(
                    text = "$unreadCount unread operational alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
            }

            if (unreadCount > 0) {
                TextButton(
                    onClick = { viewModel.markAllNotificationsRead() },
                    modifier = Modifier.testTag("btn_mark_all_read")
                ) {
                    Text("Mark all read", fontSize = 12.sp, color = LuxoraDeepEmerald, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications at this time.", color = LuxoraSlateGray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClick = { viewModel.markNotificationAsRead(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationEntity,
    onClick: () -> Unit
) {
    val dateReadable = remember(item.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.US).format(Date(item.timestamp))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isRead) LuxoraPureWhite else LuxoraWarmIvory),
        border = BorderStroke(1.dp, if (item.isRead) LuxoraBorderLight else LuxoraChampagneGold),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!item.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LuxoraChampagneDark)
                        .padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = LuxoraMidnightNavy
                    )
                    Text(dateReadable, fontSize = 10.sp, color = LuxoraSlateGray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isRead) LuxoraSlateGray else LuxoraMidnightNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusBadge(item.type, LuxoraDeepEmerald.copy(alpha = 0.1f), LuxoraDeepEmerald)
            }
        }
    }
}
