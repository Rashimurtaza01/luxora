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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.housekeepingTasks.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showNewTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedStatusFilter) {
        if (selectedStatusFilter == null) tasks
        else tasks.filter { it.status == selectedStatusFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewTaskDialog = true },
                containerColor = LuxoraDeepEmerald,
                contentColor = LuxoraPureWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Task")
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

            // Status Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("All (${tasks.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxoraMidnightNavy,
                            selectedLabelColor = LuxoraPureWhite
                        )
                    )
                }
                items(TaskStatus.values()) { st ->
                    val count = tasks.count { it.status == st }
                    FilterChip(
                        selected = selectedStatusFilter == st,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == st) null else st },
                        label = { Text("${st.label} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxoraMidnightNavy,
                            selectedLabelColor = LuxoraPureWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No housekeeping tasks under this status.", color = LuxoraSlateGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        HousekeepingTaskCard(
                            task = task,
                            onAdvance = { viewModel.advanceHousekeepingTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showNewTaskDialog) {
        NewTaskDialog(
            rooms = rooms,
            onDismiss = { showNewTaskDialog = false },
            onConfirm = { roomId, roomNumber, taskType, priority, notes ->
                viewModel.createHousekeepingTask(roomId, roomNumber, taskType, priority, notes)
                showNewTaskDialog = false
            }
        )
    }
}

@Composable
private fun HousekeepingTaskCard(
    task: HousekeepingTaskEntity,
    onAdvance: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("task_card_${task.id}")
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
                            text = "ROOM ${task.roomNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LuxoraChampagneGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.taskType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraMidnightNavy
                    )
                }

                val (pBg, pFg) = when (task.priority) {
                    TaskPriority.URGENT -> LuxoraErrorContainer to LuxoraErrorBurgundy
                    TaskPriority.HIGH -> LuxoraWarningContainer to LuxoraWarningAmber
                    TaskPriority.STANDARD -> LuxoraInfoContainer to LuxoraInfoBlue
                    TaskPriority.LOW -> LuxoraBorderLight to LuxoraSlateGray
                }
                StatusBadge(task.priority.label, pBg, pFg)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Checklist preview
            Text(
                text = "Checklist: ${task.checklistItems}",
                style = MaterialTheme.typography.bodySmall,
                color = LuxoraSlateGray,
                fontSize = 11.sp
            )

            if (task.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${task.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraMidnightNavy,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Assigned To: ${task.assignedStaffName}",
                style = MaterialTheme.typography.bodySmall,
                color = LuxoraDeepEmerald,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${task.status.label}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )

                if (task.status != TaskStatus.COMPLETED) {
                    Button(
                        onClick = onAdvance,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_advance_task_${task.id}")
                    ) {
                        val label = when (task.status) {
                            TaskStatus.PENDING -> "Start Cleaning"
                            TaskStatus.IN_PROGRESS -> "Mark Inspected"
                            TaskStatus.INSPECTED -> "Mark Ready & Clean"
                            TaskStatus.COMPLETED -> "Done"
                        }
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LuxoraSuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ready For Occupancy", fontSize = 11.sp, color = LuxoraSuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewTaskDialog(
    rooms: List<RoomWithDetails>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, TaskPriority, String) -> Unit
) {
    var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.room?.id ?: 1L) }
    var taskType by remember { mutableStateOf("Turnover Clean") }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }
    var notes by remember { mutableStateOf("") }

    val taskTypes = listOf("Turnover Clean", "Daily Service", "VIP Turndown", "Deep Clean Sanitization")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Housekeeping Task", fontWeight = FontWeight.Bold) },
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
                        Text("Room ${r.room.roomNumber} (${r.room.cleanlinessStatus.label})", fontSize = 13.sp)
                    }
                }

                Text("Task Type", style = MaterialTheme.typography.labelSmall)
                taskTypes.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskType = type }
                    ) {
                        RadioButton(
                            selected = taskType == type,
                            onClick = { taskType = type },
                            colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(type, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rooms.find { it.room.id == selectedRoomId }
                    onConfirm(selectedRoomId, r?.room?.roomNumber ?: "101", taskType, priority, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_task")
            ) {
                Text("Assign Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
