package com.example.data.repository

import com.example.data.database.LuxoraDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LuxoraRepository(private val db: LuxoraDatabase) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Default to Super Admin for seamless initial access
        CoroutineScope(Dispatchers.IO).launch {
            val adminUser = db.userDao().getUserByUsername("admin")
            _currentUser.value = adminUser
        }
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
    }

    suspend fun login(username: String, password: String):Result<UserEntity> {
        val user = db.userDao().getUserByUsername(username.trim())
        return if (user != null && user.passwordHash == password) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Config
    val configFlow: Flow<HotelConfigEntity?> = db.configDao().getConfigFlow()

    suspend fun updateConfig(config: HotelConfigEntity) {
        db.configDao().insertOrUpdate(config)
        logAudit("CONFIG_UPDATED", "Hotel configuration updated")
    }

    // Users
    val allUsersFlow: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()

    // Rooms & Types
    val allRoomTypesFlow: Flow<List<RoomTypeEntity>> = db.roomDao().getAllRoomTypesFlow()
    val allRoomsFlow: Flow<List<RoomWithDetails>> = db.roomDao().getAllRoomsWithDetailsFlow()

    suspend fun createRoom(room: RoomEntity) {
        db.roomDao().insertRoom(room)
        logAudit("ROOM_CREATED", "Room ${room.roomNumber} created")
    }

    suspend fun updateRoom(room: RoomEntity) {
        db.roomDao().updateRoom(room)
        logAudit("ROOM_UPDATED", "Room ${room.roomNumber} updated")
    }

    suspend fun updateRoomOperationalStatus(roomId: Long, status: RoomStatus, cleanliness: CleanlinessStatus) {
        db.roomDao().updateRoomStatus(roomId, status, cleanliness)
        logAudit("ROOM_STATUS_CHANGED", "Room ID $roomId status changed to $status / $cleanliness")
    }

    // Guests
    val allGuestsFlow: Flow<List<GuestEntity>> = db.guestDao().getAllGuestsFlow()

    suspend fun createGuest(guest: GuestEntity): Long {
        val id = db.guestDao().insertGuest(guest)
        logAudit("GUEST_CREATED", "Guest ${guest.fullName} registered")
        return id
    }

    suspend fun updateGuest(guest: GuestEntity) {
        db.guestDao().updateGuest(guest)
        logAudit("GUEST_UPDATED", "Guest ${guest.fullName} profile updated")
    }

    // Reservations
    val allReservationsFlow: Flow<List<ReservationWithDetails>> = db.reservationDao().getAllReservationsWithDetailsFlow()

    suspend fun createReservation(
        guestId: Long,
        roomId: Long,
        roomTypeId: Long,
        checkInDate: String,
        checkOutDate: String,
        nights: Int,
        adults: Int,
        children: Int,
        nightlyRate: Double,
        specialRequests: String
    ): Result<ReservationEntity> {
        // Prevent double booking
        val conflicts = db.reservationDao().getConflictingReservations(roomId, checkInDate, checkOutDate)
        if (conflicts.isNotEmpty()) {
            return Result.failure(Exception("Room is already booked for these selected dates."))
        }

        val config = db.configDao().getConfig() ?: HotelConfigEntity()
        val subtotal = nightlyRate * nights
        val taxAmount = subtotal * (config.taxRatePercent / 100.0)
        val grandTotal = subtotal + taxAmount
        val bookingRef = "LX-" + SimpleDateFormat("yyyy", Locale.US).format(Date()) + "-" + (1000..9999).random()

        val reservation = ReservationEntity(
            bookingReference = bookingRef,
            guestId = guestId,
            roomId = roomId,
            roomTypeId = roomTypeId,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            nights = nights,
            adults = adults,
            children = children,
            nightlyRate = nightlyRate,
            taxesAmount = taxAmount,
            discountAmount = 0.0,
            totalAmount = grandTotal,
            depositPaid = 0.0,
            balanceDue = grandTotal,
            status = ReservationStatus.CONFIRMED,
            specialRequests = specialRequests
        )

        val id = db.reservationDao().insertReservation(reservation)
        // Also create initial Room Stay folio item
        db.billingDao().insertFolioItem(
            FolioItemEntity(
                reservationId = id,
                guestId = guestId,
                category = "Room Stay",
                description = "Nightly Room Charges ($nights nights @ ${config.currencySymbol}$nightlyRate)",
                unitPrice = nightlyRate,
                quantity = nights,
                totalAmount = subtotal,
                dateString = checkInDate,
                addedBy = _currentUser.value?.fullName ?: "Front Desk"
            )
        )

        logAudit("RESERVATION_CREATED", "Booking $bookingRef created for room ID $roomId")
        return Result.success(reservation.copy(id = id))
    }

    suspend fun checkInGuest(reservationId: Long): Result<Unit> {
        val resWithDetails = db.reservationDao().getReservationById(reservationId) ?: return Result.failure(Exception("Reservation not found"))
        val res = resWithDetails.reservation
        val room = resWithDetails.room ?: return Result.failure(Exception("Room not found"))
        val guest = resWithDetails.guest ?: return Result.failure(Exception("Guest not found"))

        // Update reservation status
        db.reservationDao().updateStatus(res.id, ReservationStatus.CHECKED_IN)
        db.reservationDao().updateKeyCard(res.id, true)

        // Update room status
        db.roomDao().updateRoom(
            room.copy(
                status = RoomStatus.OCCUPIED,
                currentGuestName = guest.fullName,
                currentReservationId = res.id
            )
        )

        logAudit("CHECK_IN", "Guest ${guest.fullName} checked into Room ${room.roomNumber}")
        return Result.success(Unit)
    }

    suspend fun checkOutGuest(reservationId: Long): Result<Unit> {
        val resWithDetails = db.reservationDao().getReservationById(reservationId) ?: return Result.failure(Exception("Reservation not found"))
        val res = resWithDetails.reservation
        val room = resWithDetails.room ?: return Result.failure(Exception("Room not found"))
        val guest = resWithDetails.guest ?: return Result.failure(Exception("Guest not found"))

        if (res.balanceDue > 0.01) {
            return Result.failure(Exception("Outstanding balance of ${res.balanceDue} must be settled before checkout."))
        }

        // Complete reservation
        db.reservationDao().updateStatus(res.id, ReservationStatus.CHECKED_OUT)
        db.reservationDao().updateKeyCard(res.id, false)

        // Set room to DIRTY for Housekeeping
        db.roomDao().updateRoom(
            room.copy(
                status = RoomStatus.AVAILABLE,
                cleanlinessStatus = CleanlinessStatus.DIRTY,
                currentGuestName = null,
                currentReservationId = null
            )
        )

        // Auto-create Housekeeping Task
        db.housekeepingDao().insertTask(
            HousekeepingTaskEntity(
                roomId = room.id,
                roomNumber = room.roomNumber,
                taskType = "Turnover Clean",
                priority = if (guest.isVip) TaskPriority.URGENT else TaskPriority.HIGH,
                status = TaskStatus.PENDING,
                assignedStaffName = "Housekeeping Staff",
                notes = "Guest ${guest.fullName} checked out. Sanitize and prepare for next arrival."
            )
        )

        // Record guest stay stats
        db.guestDao().recordGuestStay(guest.id, res.totalAmount)

        logAudit("CHECK_OUT", "Guest ${guest.fullName} checked out of Room ${room.roomNumber}")
        return Result.success(Unit)
    }

    suspend fun cancelReservation(reservationId: Long): Result<Unit> {
        val resWithDetails = db.reservationDao().getReservationById(reservationId) ?: return Result.failure(Exception("Reservation not found"))
        db.reservationDao().updateStatus(reservationId, ReservationStatus.CANCELLED)
        resWithDetails.room?.let { room ->
            if (room.currentReservationId == reservationId) {
                db.roomDao().updateRoom(room.copy(status = RoomStatus.AVAILABLE, currentGuestName = null, currentReservationId = null))
            }
        }
        logAudit("RESERVATION_CANCELLED", "Booking ${resWithDetails.reservation.bookingReference} cancelled")
        return Result.success(Unit)
    }

    // Folio & Billing
    fun getFolioItemsFlow(reservationId: Long): Flow<List<FolioItemEntity>> = db.billingDao().getFolioItemsFlow(reservationId)
    fun getPaymentsFlow(reservationId: Long): Flow<List<PaymentEntity>> = db.billingDao().getPaymentsFlow(reservationId)
    val allPaymentsFlow: Flow<List<PaymentEntity>> = db.billingDao().getAllPaymentsFlow()
    val allInvoicesFlow: Flow<List<InvoiceEntity>> = db.billingDao().getAllInvoicesFlow()

    suspend fun addFolioCharge(reservationId: Long, category: String, description: String, amount: Double, qty: Int = 1) {
        val resWithDetails = db.reservationDao().getReservationById(reservationId) ?: return
        val res = resWithDetails.reservation
        val totalCharge = amount * qty
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        db.billingDao().insertFolioItem(
            FolioItemEntity(
                reservationId = reservationId,
                guestId = res.guestId,
                category = category,
                description = description,
                unitPrice = amount,
                quantity = qty,
                totalAmount = totalCharge,
                dateString = today,
                addedBy = _currentUser.value?.fullName ?: "Front Desk"
            )
        )

        val updatedTotal = res.totalAmount + totalCharge
        val updatedBalance = res.balanceDue + totalCharge
        db.reservationDao().updateReservation(res.copy(totalAmount = updatedTotal, balanceDue = updatedBalance))
        logAudit("FOLIO_CHARGE_ADDED", "$category charge of $totalCharge added to booking ${res.bookingReference}")
    }

    suspend fun recordPayment(reservationId: Long, amount: Double, method: PaymentMethod, notes: String): Result<Unit> {
        val resWithDetails = db.reservationDao().getReservationById(reservationId) ?: return Result.failure(Exception("Reservation not found"))
        val res = resWithDetails.reservation
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val txnRef = "TXN-${method.name.take(2)}-${(10000..99999).random()}"

        db.billingDao().insertPayment(
            PaymentEntity(
                reservationId = reservationId,
                guestId = res.guestId,
                amount = amount,
                method = method,
                status = PaymentStatus.COMPLETED,
                transactionRef = txnRef,
                notes = notes,
                dateString = today,
                receivedBy = _currentUser.value?.fullName ?: "Front Desk Cashier"
            )
        )

        val newDeposit = res.depositPaid + amount
        val newBalance = (res.totalAmount - newDeposit).coerceAtLeast(0.0)
        db.reservationDao().updatePaymentBalances(reservationId, newDeposit, newBalance)

        // Generate / Update Invoice
        val guest = resWithDetails.guest
        val room = resWithDetails.room
        val invNumber = "INV-2026-" + (1000 + reservationId)
        val existingInvoice = db.billingDao().getInvoiceByReservationId(reservationId)
        if (existingInvoice != null) {
            db.billingDao().updateInvoice(
                existingInvoice.copy(
                    paidAmount = newDeposit,
                    balanceDue = newBalance,
                    isPaid = newBalance <= 0.01
                )
            )
        } else {
            db.billingDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = invNumber,
                    reservationId = reservationId,
                    guestId = res.guestId,
                    guestName = guest?.fullName ?: "Guest",
                    roomNumber = room?.roomNumber ?: "N/A",
                    issueDate = today,
                    subtotal = res.totalAmount - res.taxesAmount,
                    taxAmount = res.taxesAmount,
                    discountAmount = res.discountAmount,
                    grandTotal = res.totalAmount,
                    paidAmount = newDeposit,
                    balanceDue = newBalance,
                    isPaid = newBalance <= 0.01
                )
            )
        }

        logAudit("PAYMENT_RECORDED", "Payment of $amount via $method recorded for booking ${res.bookingReference}")
        return Result.success(Unit)
    }

    // Housekeeping
    val allHousekeepingTasksFlow: Flow<List<HousekeepingTaskEntity>> = db.housekeepingDao().getAllTasksFlow()

    suspend fun advanceHousekeepingTask(task: HousekeepingTaskEntity) {
        val nextStatus = when (task.status) {
            TaskStatus.PENDING -> TaskStatus.IN_PROGRESS
            TaskStatus.IN_PROGRESS -> TaskStatus.INSPECTED
            TaskStatus.INSPECTED -> TaskStatus.COMPLETED
            TaskStatus.COMPLETED -> TaskStatus.COMPLETED
        }

        val completedTime = if (nextStatus == TaskStatus.COMPLETED) System.currentTimeMillis() else null
        db.housekeepingDao().updateTaskStatus(task.id, nextStatus, completedTime)

        // When task is completed, room cleanliness becomes READY and status becomes AVAILABLE if not occupied
        if (nextStatus == TaskStatus.COMPLETED) {
            val room = db.roomDao().getRoomById(task.roomId)
            if (room != null && room.status != RoomStatus.OCCUPIED) {
                db.roomDao().updateRoomStatus(room.id, RoomStatus.AVAILABLE, CleanlinessStatus.READY)
            }
        } else if (nextStatus == TaskStatus.IN_PROGRESS) {
            val room = db.roomDao().getRoomById(task.roomId)
            if (room != null && room.status != RoomStatus.OCCUPIED) {
                db.roomDao().updateRoomStatus(room.id, room.status, CleanlinessStatus.CLEANING)
            }
        }
        logAudit("HOUSEKEEPING_ADVANCED", "Task #${task.id} for Room ${task.roomNumber} moved to $nextStatus")
    }

    suspend fun createHousekeepingTask(task: HousekeepingTaskEntity) {
        db.housekeepingDao().insertTask(task)
        logAudit("TASK_CREATED", "Housekeeping task for Room ${task.roomNumber} created")
    }

    // Maintenance
    val allMaintenanceRequestsFlow: Flow<List<MaintenanceRequestEntity>> = db.maintenanceDao().getAllRequestsFlow()

    suspend fun createMaintenanceRequest(req: MaintenanceRequestEntity) {
        db.maintenanceDao().insertRequest(req)
        db.roomDao().updateRoomStatus(req.roomId, RoomStatus.MAINTENANCE, CleanlinessStatus.DIRTY)
        logAudit("MAINTENANCE_LOGGED", "Maintenance request for Room ${req.roomNumber}: ${req.title}")
    }

    suspend fun resolveMaintenanceRequest(req: MaintenanceRequestEntity, notes: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        db.maintenanceDao().updateRequest(
            req.copy(
                status = MaintenanceStatus.RESOLVED,
                resolvedDate = today,
                resolutionNotes = notes
            )
        )
        // Set room to AVAILABLE & READY
        db.roomDao().updateRoomStatus(req.roomId, RoomStatus.AVAILABLE, CleanlinessStatus.READY)
        logAudit("MAINTENANCE_RESOLVED", "Maintenance for Room ${req.roomNumber} resolved: $notes")
    }

    // Staff
    val allStaffFlow: Flow<List<StaffEntity>> = db.staffDao().getAllStaffFlow()

    suspend fun updateStaffStatus(staff: StaffEntity, newStatus: StaffStatus) {
        db.staffDao().updateStaff(staff.copy(status = newStatus))
        logAudit("STAFF_STATUS", "${staff.name} status updated to ${newStatus.label}")
    }

    // Inventory
    val allInventoryFlow: Flow<List<InventoryItemEntity>> = db.inventoryDao().getAllInventoryFlow()

    suspend fun restockInventoryItem(itemId: Long, qty: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        db.inventoryDao().restockItem(itemId, qty, today)
        logAudit("INVENTORY_RESTOCKED", "Item #$itemId restocked by +$qty units")
    }

    // Expenses
    val allExpensesFlow: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpensesFlow()

    suspend fun createExpense(expense: ExpenseEntity) {
        db.expenseDao().insertExpense(expense)
        logAudit("EXPENSE_RECORDED", "Expense: ${expense.category} - ${expense.description} ($${expense.amount})")
    }

    // Notifications
    val allNotificationsFlow: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotificationsFlow()
    val unreadNotificationsCountFlow: Flow<Int> = db.notificationDao().getUnreadCountFlow()

    suspend fun markNotificationAsRead(id: Long) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        db.notificationDao().markAllAsRead()
    }

    // Audit
    val auditLogsFlow: Flow<List<AuditLogEntity>> = db.auditLogDao().getRecentAuditLogsFlow()

    private suspend fun logAudit(action: String, details: String) {
        val user = _currentUser.value?.fullName ?: "System User"
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                action = action,
                details = details,
                performedBy = user,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Reset Demo Data
    suspend fun resetDemoDatabase() {
        db.clearAllTables()
        LuxoraDatabase.seedInitialData(db)
        _currentUser.value = db.userDao().getUserByUsername("admin")
    }
}
