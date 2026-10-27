package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LuxoraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LuxoraScreen(val title: String, val iconName: String) {
    DASHBOARD("Executive Dashboard", "Dashboard"),
    FRONT_DESK("Front Desk", "HotelClass"),
    RESERVATIONS("Reservations", "EventNote"),
    ROOMS("Rooms & Suites", "MeetingRoom"),
    GUESTS("Guest CRM", "People"),
    HOUSEKEEPING("Housekeeping", "CleaningServices"),
    MAINTENANCE("Maintenance", "Build"),
    BILLING("Billing & Folio", "ReceiptLong"),
    STAFF("Staff & Rosters", "Badge"),
    INVENTORY("Inventory", "Inventory"),
    EXPENSES("Expenses", "AccountBalance"),
    REPORTS("Reports & Analytics", "InsertChart"),
    NOTIFICATIONS("Notifications", "Notifications"),
    SETTINGS("Hotel Settings", "Settings")
}

data class ExecutiveMetrics(
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val availableRooms: Int = 0,
    val maintenanceRooms: Int = 0,
    val cleaningRooms: Int = 0,
    val occupancyPercent: Int = 0,
    val todayCheckIns: Int = 0,
    val todayCheckOuts: Int = 0,
    val todayRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val adr: Double = 0.0,
    val revPar: Double = 0.0
)

class LuxoraViewModel(private val repository: LuxoraRepository) : ViewModel() {

    // Current Screen
    private val _currentScreen = MutableStateFlow(LuxoraScreen.DASHBOARD)
    val currentScreen: StateFlow<LuxoraScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: LuxoraScreen) {
        _currentScreen.value = screen
    }

    // UI Message / Toast / SnackBar state
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Active User
    val currentUser = repository.currentUser
    val allUsers = repository.allUsersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchUser(user: UserEntity) {
        repository.switchUser(user)
        showMessage("Switched active profile to ${user.fullName} (${user.role.displayName})")
    }

    // Config
    val config = repository.configFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HotelConfigEntity())

    fun updateConfig(updated: HotelConfigEntity) {
        viewModelScope.launch {
            repository.updateConfig(updated)
            showMessage("Hotel configurations updated successfully")
        }
    }

    // Rooms & Room Types
    val rooms = repository.allRoomsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val roomTypes = repository.allRoomTypesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Guests
    val guests = repository.allGuestsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reservations
    val reservations = repository.allReservationsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Housekeeping & Maintenance
    val housekeepingTasks = repository.allHousekeepingTasksFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val maintenanceRequests = repository.allMaintenanceRequestsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Staff & Inventory
    val staffList = repository.allStaffFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inventoryList = repository.allInventoryFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Billing & Expenses
    val payments = repository.allPaymentsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val invoices = repository.allInvoicesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.allExpensesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications & Audit
    val notifications = repository.allNotificationsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotificationsCount = repository.unreadNotificationsCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val auditLogs = repository.auditLogsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed Executive Metrics
    val executiveMetrics: StateFlow<ExecutiveMetrics> = combine(
        rooms,
        reservations,
        payments
    ) { roomList, resList, paymentList ->
        val total = roomList.size
        val occupied = roomList.count { it.room.status == RoomStatus.OCCUPIED }
        val available = roomList.count { it.room.status == RoomStatus.AVAILABLE }
        val maintenance = roomList.count { it.room.status == RoomStatus.MAINTENANCE }
        val cleaning = roomList.count { it.room.cleanlinessStatus == CleanlinessStatus.DIRTY || it.room.cleanlinessStatus == CleanlinessStatus.CLEANING }
        val occupancyPct = if (total > 0) (occupied * 100) / total else 0

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val todayCheckIns = resList.count { it.reservation.checkInDate == todayStr && it.reservation.status != ReservationStatus.CANCELLED }
        val todayCheckOuts = resList.count { it.reservation.checkOutDate == todayStr && it.reservation.status != ReservationStatus.CANCELLED }

        val todayRevenue = paymentList.filter { it.dateString == todayStr }.sumOf { it.amount }
        val monthlyRevenue = paymentList.sumOf { it.amount }
        val outstanding = resList.filter { it.reservation.status == ReservationStatus.CHECKED_IN || it.reservation.status == ReservationStatus.CONFIRMED }
            .sumOf { it.reservation.balanceDue }

        val totalOccupiedRevenue = resList.filter { it.reservation.status == ReservationStatus.CHECKED_IN }
            .sumOf { it.reservation.nightlyRate }

        val adr = if (occupied > 0) totalOccupiedRevenue / occupied else 0.0
        val revPar = if (total > 0) totalOccupiedRevenue / total else 0.0

        ExecutiveMetrics(
            totalRooms = total,
            occupiedRooms = occupied,
            availableRooms = available,
            maintenanceRooms = maintenance,
            cleaningRooms = cleaning,
            occupancyPercent = occupancyPct,
            todayCheckIns = todayCheckIns,
            todayCheckOuts = todayCheckOuts,
            todayRevenue = todayRevenue,
            monthlyRevenue = monthlyRevenue,
            outstandingBalance = outstanding,
            adr = adr,
            revPar = revPar
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExecutiveMetrics())

    // Folio items for selected reservation
    private val _selectedReservationId = MutableStateFlow<Long?>(null)
    val selectedReservationId: StateFlow<Long?> = _selectedReservationId.asStateFlow()

    fun selectReservationForFolio(resId: Long?) {
        _selectedReservationId.value = resId
    }

    val selectedFolioItems: StateFlow<List<FolioItemEntity>> = _selectedReservationId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getFolioItemsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPayments: StateFlow<List<PaymentEntity>> = _selectedReservationId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getPaymentsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ACTIONS

    fun createReservation(
        guestId: Long,
        roomId: Long,
        roomTypeId: Long,
        checkInDate: String,
        checkOutDate: String,
        nights: Int,
        adults: Int,
        children: Int,
        nightlyRate: Double,
        specialRequests: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createReservation(
                guestId = guestId,
                roomId = roomId,
                roomTypeId = roomTypeId,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                nights = nights,
                adults = adults,
                children = children,
                nightlyRate = nightlyRate,
                specialRequests = specialRequests
            )
            result.onSuccess {
                showMessage("Reservation ${it.bookingReference} confirmed successfully!")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Failed to create reservation")
            }
        }
    }

    fun checkInGuest(reservationId: Long) {
        viewModelScope.launch {
            val res = repository.checkInGuest(reservationId)
            res.onSuccess {
                showMessage("Guest checked in successfully! Key card issued.")
            }.onFailure {
                showMessage(it.message ?: "Check-in failed")
            }
        }
    }

    fun checkOutGuest(reservationId: Long) {
        viewModelScope.launch {
            val res = repository.checkOutGuest(reservationId)
            res.onSuccess {
                showMessage("Express check-out completed! Room marked for Housekeeping.")
            }.onFailure {
                showMessage(it.message ?: "Check-out failed")
            }
        }
    }

    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            repository.cancelReservation(reservationId)
            showMessage("Reservation cancelled.")
        }
    }

    fun addFolioCharge(resId: Long, category: String, description: String, amount: Double, qty: Int = 1) {
        viewModelScope.launch {
            repository.addFolioCharge(resId, category, description, amount, qty)
            showMessage("$category charge of $${amount * qty} added to guest folio.")
        }
    }

    fun recordPayment(resId: Long, amount: Double, method: PaymentMethod, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.recordPayment(resId, amount, method, notes)
            res.onSuccess {
                showMessage("Payment of $amount via ${method.label} recorded. Folio updated.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Payment failed")
            }
        }
    }

    fun advanceHousekeepingTask(task: HousekeepingTaskEntity) {
        viewModelScope.launch {
            repository.advanceHousekeepingTask(task)
            showMessage("Room ${task.roomNumber} task progressed.")
        }
    }

    fun createHousekeepingTask(roomId: Long, roomNumber: String, taskType: String, priority: TaskPriority, notes: String) {
        viewModelScope.launch {
            repository.createHousekeepingTask(
                HousekeepingTaskEntity(
                    roomId = roomId,
                    roomNumber = roomNumber,
                    taskType = taskType,
                    priority = priority,
                    notes = notes
                )
            )
            showMessage("Housekeeping task created for Room $roomNumber")
        }
    }

    fun createMaintenanceRequest(roomId: Long, roomNumber: String, title: String, description: String, category: String, severity: MaintenanceSeverity) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.createMaintenanceRequest(
                MaintenanceRequestEntity(
                    roomId = roomId,
                    roomNumber = roomNumber,
                    title = title,
                    description = description,
                    category = category,
                    severity = severity,
                    reportedBy = currentUser.value?.fullName ?: "Staff",
                    reportedDate = today
                )
            )
            showMessage("Maintenance ticket logged for Room $roomNumber")
        }
    }

    fun resolveMaintenanceRequest(req: MaintenanceRequestEntity, notes: String) {
        viewModelScope.launch {
            repository.resolveMaintenanceRequest(req, notes)
            showMessage("Maintenance ticket #${req.id} marked as RESOLVED. Room is ready.")
        }
    }

    fun createGuest(guest: GuestEntity, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createGuest(guest)
            showMessage("Guest ${guest.fullName} registered.")
            onSuccess(id)
        }
    }

    fun updateGuest(guest: GuestEntity) {
        viewModelScope.launch {
            repository.updateGuest(guest)
            showMessage("Guest ${guest.fullName} updated.")
        }
    }

    fun createRoom(room: RoomEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createRoom(room)
            showMessage("Room ${room.roomNumber} added to inventory.")
            onSuccess()
        }
    }

    fun updateRoomStatus(roomId: Long, status: RoomStatus, cleanliness: CleanlinessStatus) {
        viewModelScope.launch {
            repository.updateRoomOperationalStatus(roomId, status, cleanliness)
            showMessage("Room status updated.")
        }
    }

    fun restockInventory(itemId: Long, qty: Int) {
        viewModelScope.launch {
            repository.restockInventoryItem(itemId, qty)
            showMessage("Inventory restocked (+${qty} units).")
        }
    }

    fun createExpense(category: String, description: String, amount: Double, method: PaymentMethod, vendor: String) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.createExpense(
                ExpenseEntity(
                    category = category,
                    description = description,
                    amount = amount,
                    paymentMethod = method,
                    dateString = today,
                    vendor = vendor,
                    approvedBy = currentUser.value?.fullName ?: "Manager"
                )
            )
            showMessage("Expense of $$amount recorded.")
        }
    }

    fun updateStaffStatus(staff: StaffEntity, newStatus: StaffStatus) {
        viewModelScope.launch {
            repository.updateStaffStatus(staff, newStatus)
            showMessage("${staff.name} is now ${newStatus.label}.")
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showMessage("All notifications marked as read.")
        }
    }

    fun resetDemoDatabase() {
        viewModelScope.launch {
            repository.resetDemoDatabase()
            showMessage("Luxora database successfully reseeded with 5-star baseline catalog!")
        }
    }
}

class LuxoraViewModelFactory(private val repository: LuxoraRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LuxoraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LuxoraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
