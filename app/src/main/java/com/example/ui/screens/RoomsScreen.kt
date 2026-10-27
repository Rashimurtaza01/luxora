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
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val roomTypes by viewModel.roomTypes.collectAsState()
    val config by viewModel.config.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<RoomStatus?>(null) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomToUpdateStatus by remember { mutableStateOf<RoomEntity?>(null) }

    val filteredRooms = remember(rooms, searchQuery, selectedStatusFilter) {
        rooms.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.room.roomNumber.contains(searchQuery.trim(), ignoreCase = true) ||
                    (item.roomType?.name?.contains(searchQuery.trim(), ignoreCase = true) == true) ||
                    (item.room.currentGuestName?.contains(searchQuery.trim(), ignoreCase = true) == true)

            val matchStatus = selectedStatusFilter == null || item.room.status == selectedStatusFilter
            matchQuery && matchStatus
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRoomDialog = true },
                containerColor = LuxoraDeepEmerald,
                contentColor = LuxoraPureWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_room")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Room")
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
                placeholder = { Text("Search by room number, type, or guest...", fontSize = 13.sp) },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_rooms")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("All (${rooms.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxoraMidnightNavy,
                            selectedLabelColor = LuxoraPureWhite
                        )
                    )
                }
                items(RoomStatus.values()) { status ->
                    val count = rooms.count { it.room.status == status }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == status) null else status
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

            // Rooms List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRooms, key = { it.room.id }) { item ->
                    RoomItemCard(
                        roomWithDetails = item,
                        currencySymbol = config?.currencySymbol ?: "$",
                        onUpdateStatusClick = { roomToUpdateStatus = item.room }
                    )
                }
            }
        }
    }

    // Add Room Dialog
    if (showAddRoomDialog) {
        AddRoomDialog(
            roomTypes = roomTypes,
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { number, floor, typeId ->
                viewModel.createRoom(
                    RoomEntity(
                        roomNumber = number,
                        floor = floor,
                        roomTypeId = typeId,
                        status = RoomStatus.AVAILABLE,
                        cleanlinessStatus = CleanlinessStatus.READY
                    )
                ) {
                    showAddRoomDialog = false
                }
            }
        )
    }

    // Update Status Dialog
    roomToUpdateStatus?.let { targetRoom ->
        UpdateRoomStatusDialog(
            room = targetRoom,
            onDismiss = { roomToUpdateStatus = null },
            onSave = { newStatus, newCleanliness ->
                viewModel.updateRoomStatus(targetRoom.id, newStatus, newCleanliness)
                roomToUpdateStatus = null
            }
        )
    }
}

@Composable
private fun RoomItemCard(
    roomWithDetails: RoomWithDetails,
    currencySymbol: String,
    onUpdateStatusClick: () -> Unit
) {
    val room = roomWithDetails.room
    val type = roomWithDetails.roomType

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("room_card_${room.roomNumber}")
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(LuxoraMidnightNavy)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ROOM ${room.roomNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LuxoraChampagneGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Floor ${room.floor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxoraSlateGray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoomStatusBadge(room.status)
                    CleanlinessBadge(room.cleanlinessStatus)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = type?.name ?: "Deluxe Suite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LuxoraMidnightNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = type?.description ?: "Luxury amenities and balcony",
                style = MaterialTheme.typography.bodySmall,
                color = LuxoraSlateGray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Amenities & Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${type?.bedType ?: "King"} • Max ${type?.capacityAdults ?: 2} Guests",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.0f", type?.basePrice ?: 450.0)} / night",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraDeepEmerald
                )
            }

            if (room.currentGuestName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = LuxoraMidnightNavy.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = LuxoraMidnightNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "In-House: ${room.currentGuestName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LuxoraMidnightNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onUpdateStatusClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    border = BorderStroke(0.8.dp, LuxoraSlateGray),
                    modifier = Modifier.testTag("btn_update_status_${room.roomNumber}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = LuxoraSlateGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Status", fontSize = 11.sp, color = LuxoraSlateGray)
                }
            }
        }
    }
}

@Composable
private fun AddRoomDialog(
    roomTypes: List<RoomTypeEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Long) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var floorStr by remember { mutableStateOf("1") }
    var selectedTypeId by remember { mutableStateOf(roomTypes.firstOrNull()?.id ?: 1L) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room to Inventory", fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number (e.g. 305)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_new_room_number")
                )

                OutlinedTextField(
                    value = floorStr,
                    onValueChange = { floorStr = it },
                    label = { Text("Floor Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Room Type", style = MaterialTheme.typography.labelMedium, color = LuxoraSlateGray)
                roomTypes.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTypeId = type.id }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTypeId == type.id,
                            onClick = { selectedTypeId = type.id },
                            colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(type.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = LuxoraErrorBurgundy, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomNumber.isBlank()) {
                        errorMsg = "Please enter room number"
                        return@Button
                    }
                    val floor = floorStr.toIntOrNull() ?: 1
                    onConfirm(roomNumber.trim(), floor, selectedTypeId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_add_room")
            ) {
                Text("Add Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun UpdateRoomStatusDialog(
    room: RoomEntity,
    onDismiss: () -> Unit,
    onSave: (RoomStatus, CleanlinessStatus) -> Unit
) {
    var status by remember { mutableStateOf(room.status) }
    var cleanliness by remember { mutableStateOf(room.cleanlinessStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Room ${room.roomNumber} Status", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Operational Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                RoomStatus.values().forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { status = st }
                    ) {
                        RadioButton(
                            selected = status == st,
                            onClick = { status = st },
                            colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(st.label)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Cleanliness:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                CleanlinessStatus.values().forEach { cl ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cleanliness = cl }
                    ) {
                        RadioButton(
                            selected = cleanliness == cl,
                            onClick = { cleanliness = cl },
                            colors = RadioButtonDefaults.colors(selectedColor = LuxoraDeepEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(cl.label)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(status, cleanliness) },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_save_room_status")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
