package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String = "",
    val active: Boolean = true
)

@Entity(tableName = "room_types", indices = [Index(value = ["code"], unique = true)])
data class RoomTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val description: String,
    val basePrice: Double,
    val capacityAdults: Int,
    val capacityChildren: Int,
    val bedType: String,
    val sizeSqMeters: Int,
    val amenities: String, // comma-separated or short description
    val imageUrl: String = ""
)

@Entity(
    tableName = "rooms",
    indices = [
        Index(value = ["roomNumber"], unique = true),
        Index(value = ["roomTypeId"]),
        Index(value = ["status"])
    ]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: String,
    val floor: Int,
    val roomTypeId: Long,
    val status: RoomStatus = RoomStatus.AVAILABLE,
    val cleanlinessStatus: CleanlinessStatus = CleanlinessStatus.READY,
    val currentGuestName: String? = null,
    val currentReservationId: Long? = null,
    val notes: String = ""
)

@Entity(
    tableName = "guests",
    indices = [
        Index(value = ["email"]),
        Index(value = ["phone"])
    ]
)
data class GuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val idOrPassport: String,
    val country: String,
    val isVip: Boolean = false,
    val preferences: String = "",
    val notes: String = "",
    val totalStays: Int = 0,
    val lifetimeSpend: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String get() = "$firstName $lastName"
}

@Entity(
    tableName = "reservations",
    indices = [
        Index(value = ["bookingReference"], unique = true),
        Index(value = ["guestId"]),
        Index(value = ["roomId"]),
        Index(value = ["status"])
    ]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingReference: String,
    val guestId: Long,
    val roomId: Long,
    val roomTypeId: Long,
    val checkInDate: String, // YYYY-MM-DD
    val checkOutDate: String, // YYYY-MM-DD
    val nights: Int,
    val adults: Int,
    val children: Int = 0,
    val nightlyRate: Double,
    val taxesAmount: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val depositPaid: Double = 0.0,
    val balanceDue: Double,
    val status: ReservationStatus = ReservationStatus.CONFIRMED,
    val specialRequests: String = "",
    val keyCardIssued: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "folio_items",
    indices = [Index(value = ["reservationId"])]
)
data class FolioItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reservationId: Long,
    val guestId: Long,
    val category: String, // Room Stay, Fine Dining, Spa & Wellness, Minibar, Laundry, Airport Transfer
    val description: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val totalAmount: Double,
    val dateString: String,
    val addedBy: String = "Front Desk",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payments",
    indices = [Index(value = ["reservationId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reservationId: Long,
    val guestId: Long,
    val amount: Double,
    val method: PaymentMethod,
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val transactionRef: String,
    val notes: String = "",
    val dateString: String,
    val receivedBy: String = "Cashier",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNumber"], unique = true), Index(value = ["reservationId"])]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val reservationId: Long,
    val guestId: Long,
    val guestName: String,
    val roomNumber: String,
    val issueDate: String,
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val grandTotal: Double,
    val paidAmount: Double,
    val balanceDue: Double,
    val isPaid: Boolean = false
)

@Entity(
    tableName = "housekeeping_tasks",
    indices = [Index(value = ["roomId"]), Index(value = ["status"])]
)
data class HousekeepingTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val roomNumber: String,
    val taskType: String = "Daily Service", // Turnover, Daily Service, Turndown, Deep Clean
    val priority: TaskPriority = TaskPriority.STANDARD,
    val status: TaskStatus = TaskStatus.PENDING,
    val assignedStaffName: String = "Staff On Duty",
    val checklistItems: String = "Bed linen changed, Bathroom sanitized, Minibar replenished, Amenities replaced",
    val notes: String = "",
    val assignedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(
    tableName = "maintenance_requests",
    indices = [Index(value = ["roomId"]), Index(value = ["status"])]
)
data class MaintenanceRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val roomNumber: String,
    val title: String,
    val description: String,
    val category: String, // HVAC, Plumbing, Electrical, Furniture, Technology
    val severity: MaintenanceSeverity = MaintenanceSeverity.MEDIUM,
    val status: MaintenanceStatus = MaintenanceStatus.PENDING,
    val reportedBy: String,
    val assignedTechnician: String = "Engineering Team",
    val reportedDate: String,
    val resolvedDate: String? = null,
    val resolutionNotes: String = ""
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val roleTitle: String,
    val department: String, // Front Office, Housekeeping, Food & Beverage, Engineering, Management, Finance
    val shift: StaffShift = StaffShift.MORNING,
    val status: StaffStatus = StaffStatus.ON_DUTY,
    val joinedDate: String
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Linen, Toiletries, F&B, Cleaning, Stationery
    val sku: String,
    val unit: String, // pcs, bottles, sets, boxes
    val currentStock: Int,
    val minStockLevel: Int,
    val unitCost: Double,
    val supplierName: String,
    val lastRestocked: String
) {
    val isLowStock: Boolean get() = currentStock <= minStockLevel
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Supplies, Utilities, Maintenance, F&B Procurement, Laundry, Marketing
    val description: String,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
    val dateString: String,
    val vendor: String,
    val approvedBy: String = "General Manager"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // Arrival, Departure, Maintenance, Inventory, Payment, VIP
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hotel_config")
data class HotelConfigEntity(
    @PrimaryKey val id: Int = 1,
    val hotelName: String = "LUXORA Grand Palace & Spa",
    val tagLine: String = "Unrivaled Luxury & Bespoke Hospitality",
    val address: String = "88 Grand Horizon Boulevard, Suite 500",
    val phone: String = "+1 (800) 589-6721",
    val email: String = "concierge@luxorahotel.com",
    val currencySymbol: String = "$",
    val currencyCode: String = "USD",
    val taxRatePercent: Double = 12.0,
    val serviceChargePercent: Double = 5.0,
    val checkInTime: String = "15:00",
    val checkOutTime: String = "12:00"
)
