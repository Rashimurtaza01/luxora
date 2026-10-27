package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxoraMonogram
import com.example.ui.theme.*
import com.example.ui.viewmodel.LuxoraScreen
import com.example.ui.viewmodel.LuxoraViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: LuxoraViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var folioJumpResId by remember { mutableStateOf<Long?>(null) }

    // Handle back button: if not on Dashboard, navigate to Dashboard; if drawer open, close it
    BackHandler(enabled = drawerState.isOpen || currentScreen != LuxoraScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen != LuxoraScreen.DASHBOARD) {
            viewModel.navigateTo(LuxoraScreen.DASHBOARD)
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = LuxoraMidnightNavy,
                drawerContentColor = LuxoraPureWhite,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        LuxoraMonogram(size = 42)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LUXORA",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp,
                                color = LuxoraChampagneGold
                            )
                            Text(
                                text = "★★★★★ PALACE & SPA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LuxoraChampagneLight
                            )
                        }
                    }

                    // Active User Profile Banner in Drawer
                    Surface(
                        color = LuxoraDarkCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(LuxoraDeepEmerald),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.fullName?.take(1) ?: "U"),
                                    color = LuxoraPureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentUser?.fullName ?: "Staff",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = LuxoraPureWhite
                                )
                                Text(
                                    text = currentUser?.role?.displayName ?: "User",
                                    fontSize = 10.sp,
                                    color = LuxoraChampagneLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "CORE OPERATIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraSlateGray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )

                    DrawerItem("Executive Dashboard", Icons.Default.Dashboard, currentScreen == LuxoraScreen.DASHBOARD) {
                        viewModel.navigateTo(LuxoraScreen.DASHBOARD)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Front Desk Operations", Icons.Default.HotelClass, currentScreen == LuxoraScreen.FRONT_DESK) {
                        viewModel.navigateTo(LuxoraScreen.FRONT_DESK)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Reservations & Booking", Icons.Default.EventNote, currentScreen == LuxoraScreen.RESERVATIONS) {
                        viewModel.navigateTo(LuxoraScreen.RESERVATIONS)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Rooms & Suites", Icons.Default.MeetingRoom, currentScreen == LuxoraScreen.ROOMS) {
                        viewModel.navigateTo(LuxoraScreen.ROOMS)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Guest CRM Directory", Icons.Default.People, currentScreen == LuxoraScreen.GUESTS) {
                        viewModel.navigateTo(LuxoraScreen.GUESTS)
                        scope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "PROPERTY & SERVICES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraSlateGray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )

                    DrawerItem("Housekeeping Board", Icons.Default.CleaningServices, currentScreen == LuxoraScreen.HOUSEKEEPING) {
                        viewModel.navigateTo(LuxoraScreen.HOUSEKEEPING)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Maintenance Requests", Icons.Default.Build, currentScreen == LuxoraScreen.MAINTENANCE) {
                        viewModel.navigateTo(LuxoraScreen.MAINTENANCE)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Billing & Guest Folio", Icons.Default.ReceiptLong, currentScreen == LuxoraScreen.BILLING) {
                        viewModel.navigateTo(LuxoraScreen.BILLING)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Staff & Rosters", Icons.Default.Badge, currentScreen == LuxoraScreen.STAFF) {
                        viewModel.navigateTo(LuxoraScreen.STAFF)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Inventory Management", Icons.Default.Inventory, currentScreen == LuxoraScreen.INVENTORY) {
                        viewModel.navigateTo(LuxoraScreen.INVENTORY)
                        scope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "EXECUTIVE & ADMIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxoraSlateGray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )

                    DrawerItem("Reports & Analytics", Icons.Default.InsertChart, currentScreen == LuxoraScreen.REPORTS) {
                        viewModel.navigateTo(LuxoraScreen.REPORTS)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem(
                        "Notifications",
                        Icons.Default.Notifications,
                        currentScreen == LuxoraScreen.NOTIFICATIONS,
                        badgeCount = unreadNotifications
                    ) {
                        viewModel.navigateTo(LuxoraScreen.NOTIFICATIONS)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Hotel Settings", Icons.Default.Settings, currentScreen == LuxoraScreen.SETTINGS) {
                        viewModel.navigateTo(LuxoraScreen.SETTINGS)
                        scope.launch { drawerState.close() }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LUXORA",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp,
                                color = LuxoraChampagneGold
                            )
                            Text(
                                text = currentScreen.title,
                                fontSize = 11.sp,
                                color = LuxoraChampagneLight.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_open_drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = LuxoraChampagneGold)
                        }
                    },
                    actions = {
                        // Notifications badge button
                        IconButton(
                            onClick = { viewModel.navigateTo(LuxoraScreen.NOTIFICATIONS) },
                            modifier = Modifier.testTag("btn_notifications_bell")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge(containerColor = LuxoraChampagneDark) {
                                            Text("$unreadNotifications", color = LuxoraPureWhite, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = LuxoraChampagneGold)
                            }
                        }

                        // Role badge chip
                        Surface(
                            color = LuxoraDarkCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(LuxoraScreen.SETTINGS) }
                                .padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuxoraSuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentUser?.role?.displayName ?: "Staff",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxoraChampagneLight
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = LuxoraMidnightNavy
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = LuxoraMidnightNavy,
                    contentColor = LuxoraChampagneGold,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == LuxoraScreen.DASHBOARD,
                        onClick = { viewModel.navigateTo(LuxoraScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuxoraMidnightNavy,
                            selectedTextColor = LuxoraChampagneGold,
                            indicatorColor = LuxoraChampagneGold,
                            unselectedIconColor = LuxoraSlateGray,
                            unselectedTextColor = LuxoraSlateGray
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == LuxoraScreen.FRONT_DESK,
                        onClick = { viewModel.navigateTo(LuxoraScreen.FRONT_DESK) },
                        icon = { Icon(Icons.Default.HotelClass, contentDescription = "Front Desk") },
                        label = { Text("Front Desk", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuxoraMidnightNavy,
                            selectedTextColor = LuxoraChampagneGold,
                            indicatorColor = LuxoraChampagneGold,
                            unselectedIconColor = LuxoraSlateGray,
                            unselectedTextColor = LuxoraSlateGray
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == LuxoraScreen.RESERVATIONS,
                        onClick = { viewModel.navigateTo(LuxoraScreen.RESERVATIONS) },
                        icon = { Icon(Icons.Default.EventNote, contentDescription = "Bookings") },
                        label = { Text("Bookings", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuxoraMidnightNavy,
                            selectedTextColor = LuxoraChampagneGold,
                            indicatorColor = LuxoraChampagneGold,
                            unselectedIconColor = LuxoraSlateGray,
                            unselectedTextColor = LuxoraSlateGray
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == LuxoraScreen.ROOMS,
                        onClick = { viewModel.navigateTo(LuxoraScreen.ROOMS) },
                        icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Rooms") },
                        label = { Text("Suites", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuxoraMidnightNavy,
                            selectedTextColor = LuxoraChampagneGold,
                            indicatorColor = LuxoraChampagneGold,
                            unselectedIconColor = LuxoraSlateGray,
                            unselectedTextColor = LuxoraSlateGray
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == LuxoraScreen.BILLING,
                        onClick = { viewModel.navigateTo(LuxoraScreen.BILLING) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Billing") },
                        label = { Text("Billing", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuxoraMidnightNavy,
                            selectedTextColor = LuxoraChampagneGold,
                            indicatorColor = LuxoraChampagneGold,
                            unselectedIconColor = LuxoraSlateGray,
                            unselectedTextColor = LuxoraSlateGray
                        )
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = LuxoraWarmIvory,
            modifier = modifier
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    LuxoraScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                    LuxoraScreen.FRONT_DESK -> FrontDeskScreen(
                        viewModel = viewModel,
                        initialFolioReservationId = folioJumpResId
                    )
                    LuxoraScreen.RESERVATIONS -> ReservationsScreen(
                        viewModel = viewModel,
                        onNavigateToFolio = { resId ->
                            folioJumpResId = resId
                            viewModel.navigateTo(LuxoraScreen.FRONT_DESK)
                        }
                    )
                    LuxoraScreen.ROOMS -> RoomsScreen(viewModel = viewModel)
                    LuxoraScreen.GUESTS -> GuestsScreen(viewModel = viewModel)
                    LuxoraScreen.HOUSEKEEPING -> HousekeepingScreen(viewModel = viewModel)
                    LuxoraScreen.MAINTENANCE -> MaintenanceScreen(viewModel = viewModel)
                    LuxoraScreen.BILLING -> BillingScreen(viewModel = viewModel)
                    LuxoraScreen.STAFF -> StaffAndInventoryScreen(viewModel = viewModel, initialTab = 0)
                    LuxoraScreen.INVENTORY -> StaffAndInventoryScreen(viewModel = viewModel, initialTab = 1)
                    LuxoraScreen.EXPENSES -> BillingScreen(viewModel = viewModel)
                    LuxoraScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                    LuxoraScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                    LuxoraScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LuxoraChampagneDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$badgeCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxoraPureWhite
                        )
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) LuxoraMidnightNavy else LuxoraChampagneGold,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = LuxoraChampagneGold,
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = LuxoraMidnightNavy,
            unselectedTextColor = LuxoraPureWhite
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
