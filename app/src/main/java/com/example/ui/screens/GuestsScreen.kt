package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.model.GuestEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestsScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val guests by viewModel.guests.collectAsState()
    val config by viewModel.config.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var vipOnlyFilter by remember { mutableStateOf(false) }
    var showAddGuestDialog by remember { mutableStateOf(false) }
    var editingGuest by remember { mutableStateOf<GuestEntity?>(null) }

    val filteredGuests = remember(guests, searchQuery, vipOnlyFilter) {
        guests.filter { g ->
            val matchQuery = searchQuery.isBlank() ||
                    g.fullName.contains(searchQuery.trim(), ignoreCase = true) ||
                    g.email.contains(searchQuery.trim(), ignoreCase = true) ||
                    g.phone.contains(searchQuery.trim(), ignoreCase = true) ||
                    g.country.contains(searchQuery.trim(), ignoreCase = true)

            val matchVip = !vipOnlyFilter || g.isVip
            matchQuery && matchVip
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGuestDialog = true },
                containerColor = LuxoraDeepEmerald,
                contentColor = LuxoraPureWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_guest")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Guest")
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
                placeholder = { Text("Search guests by name, email, country, or phone...", fontSize = 13.sp) },
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
                modifier = Modifier.fillMaxWidth().testTag("input_search_guests")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !vipOnlyFilter,
                    onClick = { vipOnlyFilter = false },
                    label = { Text("All Guests (${guests.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuxoraMidnightNavy,
                        selectedLabelColor = LuxoraPureWhite
                    )
                )
                FilterChip(
                    selected = vipOnlyFilter,
                    onClick = { vipOnlyFilter = !vipOnlyFilter },
                    label = { Text("VIP Patrons Only (${guests.count { it.isVip }})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuxoraChampagneDark,
                        selectedLabelColor = LuxoraPureWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredGuests, key = { it.id }) { guest ->
                    GuestProfileCard(
                        guest = guest,
                        currencySymbol = config?.currencySymbol ?: "$",
                        onEdit = { editingGuest = guest }
                    )
                }
            }
        }
    }

    // Add / Edit Guest Dialog
    if (showAddGuestDialog || editingGuest != null) {
        val target = editingGuest
        GuestFormDialog(
            initialGuest = target,
            onDismiss = {
                showAddGuestDialog = false
                editingGuest = null
            },
            onSave = { updatedGuest ->
                if (target == null) {
                    viewModel.createGuest(updatedGuest) {}
                } else {
                    viewModel.updateGuest(updatedGuest)
                }
                showAddGuestDialog = false
                editingGuest = null
            }
        )
    }
}

@Composable
private fun GuestProfileCard(
    guest: GuestEntity,
    currencySymbol: String,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxoraPureWhite),
        border = BorderStroke(1.dp, LuxoraBorderLight),
        modifier = Modifier.fillMaxWidth().testTag("guest_card_${guest.id}")
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (guest.isVip) LuxoraChampagneDark else LuxoraMidnightNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${guest.firstName.take(1)}${guest.lastName.take(1)}",
                            fontWeight = FontWeight.Bold,
                            color = LuxoraPureWhite,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = guest.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LuxoraMidnightNavy
                            )
                            if (guest.isVip) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge("VIP PATRON", LuxoraChampagneLight, LuxoraMidnightNavy)
                            }
                        }
                        Text(
                            text = "${guest.country} • ID: ${guest.idOrPassport}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxoraSlateGray,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = LuxoraSlateGray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(guest.email, style = MaterialTheme.typography.bodySmall, color = LuxoraDeepEmerald)
                Text(guest.phone, style = MaterialTheme.typography.bodySmall, color = LuxoraSlateGray)
            }

            // Preferences
            if (guest.preferences.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = LuxoraWarmIvory,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = LuxoraChampagneDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preferences: ${guest.preferences}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxoraMidnightNavy,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxoraBorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            // Loyalty stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Completed Stays: ${guest.totalStays}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxoraSlateGray
                )
                Text(
                    text = "Lifetime Spend: $currencySymbol${String.format(Locale.US, "%,.2f", guest.lifetimeSpend)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = LuxoraMidnightNavy
                )
            }
        }
    }
}

@Composable
private fun GuestFormDialog(
    initialGuest: GuestEntity?,
    onDismiss: () -> Unit,
    onSave: (GuestEntity) -> Unit
) {
    var firstName by remember { mutableStateOf(initialGuest?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialGuest?.lastName ?: "") }
    var email by remember { mutableStateOf(initialGuest?.email ?: "") }
    var phone by remember { mutableStateOf(initialGuest?.phone ?: "") }
    var idOrPassport by remember { mutableStateOf(initialGuest?.idOrPassport ?: "") }
    var country by remember { mutableStateOf(initialGuest?.country ?: "") }
    var isVip by remember { mutableStateOf(initialGuest?.isVip ?: false) }
    var preferences by remember { mutableStateOf(initialGuest?.preferences ?: "") }
    var notes by remember { mutableStateOf(initialGuest?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGuest == null) "Register New Guest" else "Edit Guest Profile", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_guest_first_name")
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_guest_last_name")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_guest_email")
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_guest_phone")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = idOrPassport,
                            onValueChange = { idOrPassport = it },
                            label = { Text("ID / Passport") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isVip,
                            onCheckedChange = { isVip = it },
                            colors = CheckboxDefaults.colors(checkedColor = LuxoraChampagneDark)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Designate as VIP Patron", fontWeight = FontWeight.Bold, color = LuxoraMidnightNavy)
                    }
                }

                item {
                    OutlinedTextField(
                        value = preferences,
                        onValueChange = { preferences = it },
                        label = { Text("Hospitality Preferences (Pillows, beverages)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Internal Staff Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        val g = (initialGuest ?: GuestEntity(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            idOrPassport = idOrPassport.trim(),
                            country = country.trim(),
                            isVip = isVip,
                            preferences = preferences.trim(),
                            notes = notes.trim()
                        )).copy(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            idOrPassport = idOrPassport.trim(),
                            country = country.trim(),
                            isVip = isVip,
                            preferences = preferences.trim(),
                            notes = notes.trim()
                        )
                        onSave(g)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxoraDeepEmerald),
                modifier = Modifier.testTag("btn_confirm_guest")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
