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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.maintenanceRequests.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var showActiveOnly by remember { mutableStateOf(true) }
    var showNewRequestDialog by remember { mutableStateOf(false) }
    var resolvingRequest by remember { mutableStateOf<MaintenanceRequestEntity?>(null) }

    val filtered = remember(requests, showActiveOnly) {
        if (showActiveOnly) requests.filter { it.status != MaintenanceStatus.RESOLVED }
        else requests
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewRequestDialog = true },
                containerColor = LuxoraDeepEmerald,
                contentColor = LuxoraPureWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_maintenance")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Ticket")
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showActiveOnly,
                    onClick = { showActiveOnly = true },
                    label = { Text("Active Tickets (${requests.count { it.status != MaintenanceStatus.RESOLVED }})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuxoraMidnightNavy,
                        selectedLabelColor = LuxoraPureWhite
                    )
                )
                FilterChip(
                    selected = !showActiveOnly,
                    onClick = { showActiveOnly = false },
                    label = { Text("All Records (${requests.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuxoraMidnightNavy,
                        selectedLabelColor = LuxoraPureWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("All maintenance systems operational. No open tickets.", color = LuxoraSlateGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { req ->
                        MaintenanceTicketCard(
                            req = req,
                            onResolve = { resolvingRequest = req }
                        )
                    }
                }
            }
        }
    }

    if (showNewRequestDialog) {
        NewMaintenanceDialog(
            rooms = rooms,
            onDismiss = { showNewRequestDialog = false },
            onConfirm = { roomId, roomNumber, title, desc, cat, sev ->
                viewModel.createMaintenanceRequest(roomId, roomNumber, title, desc, cat, sev)
                showNewRequestDialog = false
            }
        )
    }

    resolvingRequest?.let { req ->
        ResolveMaintenanceDialog(
            req = req,
            onDismiss = { resolvingRequest = null },
            onConfirm = { notes ->
                viewModel.resolveMaintenanceRequest(req, notes)
                resolvingRequest = null
            }
        )
    }
}

@Composable
private fun MaintenanceTicketCard(
    req: MaintenanceRequestEntity,
    onResolve: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("ticket_card_${req.id}")
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
                            text = "ROOM ${req.roomNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LuxoraChampagneGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = req.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxoraDeepEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }

                val (sBg, sFg) = when (req.severity) {
                    MaintenanceSeverity.CRITICAL -> LuxoraErrorContainer to LuxoraErrorBurgundy
                    MaintenanceSeverity.HIGH -> LuxoraErrorContainer to LuxoraErrorBurgundy
                    MaintenanceSeverity.MEDIUM -> LuxoraWarningContainer to LuxoraWarningAmber
                    MaintenanceSeverity.LOW -> LuxoraBorderLight to LuxoraSlateGray
                }
                StatusBadge(req.severity.label, sBg, sFg)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = req.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LuxoraMidnightNavy
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = req.description,
                style = MaterialTheme.typography.bodySmall,
                color = LuxoraSlateGray
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reported: ${req.reportedDate} by ${req.reportedBy}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = LuxoraSlateGray
                )
                Text(
                    text = "Tech: ${req.assignedTechnician}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = LuxoraDeepEmerald
                )
            }

            if (req.resolutionNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = LuxoraSuccessContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Resolved (${req.resolvedDate}): ${req.resolutionNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxoraSuccessGreen,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 11.sp
                    )
                }
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
                    text = "Status: ${req.status.label}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )

                if (req.status != MaintenanceStatus.RESOLVED) {
                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_resolve_ticket_${req.id}")
                    ) {
                        Text("Resolve Ticket", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewMaintenanceDialog(
    rooms: List<RoomWithDetails>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String, String, MaintenanceSeverity) -> Unit
) {
    var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.room?.id ?: 1L) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("HVAC") }
    var severity by remember { mutableStateOf(MaintenanceSeverity.HIGH) }

    val categories = listOf("HVAC", "Plumbing", "Electrical", "Furniture", "Technology")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Maintenance Ticket", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Room", style = MaterialTheme.typography.labelSmall)
                rooms.take(6).forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRoomId = r.room.id }
                    ) {
                        RadioButton(
                            selected = selectedRoomId == r.room.id,
                            onClick = { selectedRoomId = r.room.id },
                            colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Room ${r.room.roomNumber} (Floor ${r.room.floor})", fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Issue Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_ticket_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description") },
                    modifier = Modifier.fillMaxWidth().testTag("input_ticket_desc")
                )

                Text("Category", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rooms.find { it.room.id == selectedRoomId }
                    if (title.isNotBlank()) {
                        onConfirm(selectedRoomId, r?.room?.roomNumber ?: "101", title.trim(), description.trim(), category, severity)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_maintenance")
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ResolveMaintenanceDialog(
    req: MaintenanceRequestEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("Inspected, repaired, and operational tests verified.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resolve Room ${req.roomNumber} Ticket", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Issue: ${req.title}", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Resolution Notes & Parts Replaced") },
                    modifier = Modifier.fillMaxWidth().testTag("input_resolve_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(notes.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_resolve")
            ) {
                Text("Confirm Resolution")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
