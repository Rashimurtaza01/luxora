package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE active = 1")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM room_types")
    fun getAllRoomTypesFlow(): Flow<List<RoomTypeEntity>>

    @Query("SELECT * FROM room_types WHERE id = :id")
    suspend fun getRoomTypeById(id: Long): RoomTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomType(type: RoomTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomTypes(types: List<RoomTypeEntity>)

    @Transaction
    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    fun getAllRoomsWithDetailsFlow(): Flow<List<RoomWithDetails>>

    @Transaction
    @Query("SELECT * FROM rooms WHERE status = :status ORDER BY roomNumber ASC")
    fun getRoomsByStatusFlow(status: RoomStatus): Flow<List<RoomWithDetails>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoomById(id: Long): RoomEntity?

    @Query("SELECT * FROM rooms WHERE roomNumber = :roomNumber LIMIT 1")
    suspend fun getRoomByNumber(roomNumber: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("UPDATE rooms SET status = :status, cleanlinessStatus = :cleanliness WHERE id = :roomId")
    suspend fun updateRoomStatus(roomId: Long, status: RoomStatus, cleanliness: CleanlinessStatus)

    @Query("UPDATE rooms SET currentGuestName = :guestName, currentReservationId = :reservationId WHERE id = :roomId")
    suspend fun updateRoomGuest(roomId: Long, guestName: String?, reservationId: Long?)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)
}

@Dao
interface GuestDao {
    @Query("SELECT * FROM guests ORDER BY createdAt DESC")
    fun getAllGuestsFlow(): Flow<List<GuestEntity>>

    @Query("SELECT * FROM guests WHERE id = :id")
    suspend fun getGuestById(id: Long): GuestEntity?

    @Query("SELECT * FROM guests WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchGuestsFlow(query: String): Flow<List<GuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: GuestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuests(guests: List<GuestEntity>)

    @Update
    suspend fun updateGuest(guest: GuestEntity)

    @Query("UPDATE guests SET totalStays = totalStays + 1, lifetimeSpend = lifetimeSpend + :amount WHERE id = :guestId")
    suspend fun recordGuestStay(guestId: Long, amount: Double)

    @Transaction
    @Query("SELECT * FROM guests WHERE id = :guestId")
    suspend fun getGuestWithReservations(guestId: Long): GuestWithReservations?
}

@Dao
interface ReservationDao {
    @Transaction
    @Query("SELECT * FROM reservations ORDER BY createdAt DESC")
    fun getAllReservationsWithDetailsFlow(): Flow<List<ReservationWithDetails>>

    @Transaction
    @Query("SELECT * FROM reservations WHERE status = :status ORDER BY createdAt DESC")
    fun getReservationsByStatusFlow(status: ReservationStatus): Flow<List<ReservationWithDetails>>

    @Transaction
    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getReservationById(id: Long): ReservationWithDetails?

    @Query("SELECT * FROM reservations WHERE bookingReference = :ref LIMIT 1")
    suspend fun getReservationByRef(ref: String): ReservationEntity?

    @Query("SELECT * FROM reservations WHERE roomId = :roomId AND status IN ('CONFIRMED', 'CHECKED_IN') AND ((checkInDate <= :checkOutDate) AND (checkOutDate >= :checkInDate))")
    suspend fun getConflictingReservations(roomId: Long, checkInDate: String, checkOutDate: String): List<ReservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservations(reservations: List<ReservationEntity>)

    @Update
    suspend fun updateReservation(reservation: ReservationEntity)

    @Query("UPDATE reservations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ReservationStatus)

    @Query("UPDATE reservations SET keyCardIssued = :issued WHERE id = :id")
    suspend fun updateKeyCard(id: Long, issued: Boolean)

    @Query("UPDATE reservations SET balanceDue = :balance, depositPaid = :deposit WHERE id = :id")
    suspend fun updatePaymentBalances(id: Long, deposit: Double, balance: Double)
}

@Dao
interface BillingDao {
    @Query("SELECT * FROM folio_items WHERE reservationId = :resId ORDER BY timestamp ASC")
    fun getFolioItemsFlow(resId: Long): Flow<List<FolioItemEntity>>

    @Query("SELECT * FROM folio_items WHERE reservationId = :resId")
    suspend fun getFolioItemsForReservation(resId: Long): List<FolioItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolioItem(item: FolioItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolioItems(items: List<FolioItemEntity>)

    @Query("SELECT * FROM payments WHERE reservationId = :resId ORDER BY timestamp DESC")
    fun getPaymentsFlow(resId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoicesFlow(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE reservationId = :resId LIMIT 1")
    suspend fun getInvoiceByReservationId(resId: Long): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)
}

@Dao
interface HousekeepingDao {
    @Query("SELECT * FROM housekeeping_tasks ORDER BY priority DESC, id DESC")
    fun getAllTasksFlow(): Flow<List<HousekeepingTaskEntity>>

    @Query("SELECT * FROM housekeeping_tasks WHERE status = :status ORDER BY priority DESC")
    fun getTasksByStatusFlow(status: TaskStatus): Flow<List<HousekeepingTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: HousekeepingTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<HousekeepingTaskEntity>)

    @Update
    suspend fun updateTask(task: HousekeepingTaskEntity)

    @Query("UPDATE housekeeping_tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: TaskStatus, completedAt: Long?)
}

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_requests ORDER BY id DESC")
    fun getAllRequestsFlow(): Flow<List<MaintenanceRequestEntity>>

    @Query("SELECT * FROM maintenance_requests WHERE status != 'RESOLVED' ORDER BY severity DESC, id DESC")
    fun getActiveRequestsFlow(): Flow<List<MaintenanceRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: MaintenanceRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<MaintenanceRequestEntity>)

    @Update
    suspend fun updateRequest(request: MaintenanceRequestEntity)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY department ASC, name ASC")
    fun getAllStaffFlow(): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStaff(list: List<StaffEntity>)

    @Update
    suspend fun updateStaff(staff: StaffEntity)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY category ASC, name ASC")
    fun getAllInventoryFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE currentStock <= minStockLevel")
    fun getLowStockItemsFlow(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET currentStock = currentStock + :quantity, lastRestocked = :date WHERE id = :id")
    suspend fun restockItem(id: Long, quantity: Int, date: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateString DESC, id DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM hotel_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<HotelConfigEntity?>

    @Query("SELECT * FROM hotel_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): HotelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: HotelConfigEntity)
}
