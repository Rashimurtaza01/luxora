package com.example.data.model

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    SUPER_ADMIN("Super Admin", 0xFF101C2C),
    HOTEL_MANAGER("Hotel Manager", 0xFF164E45),
    RECEPTIONIST("Receptionist", 0xFFC5A46D),
    ACCOUNTANT("Accountant", 0xFF1D5AAB),
    HOUSEKEEPING("Housekeeping", 0xFF23865B)
}

enum class RoomStatus(val label: String, val colorHex: Long) {
    AVAILABLE("Available", 0xFF23865B),
    RESERVED("Reserved", 0xFFC5A46D),
    OCCUPIED("Occupied", 0xFF101C2C),
    MAINTENANCE("Maintenance", 0xFFB54755),
    OUT_OF_ORDER("Out of Order", 0xFF697586)
}

enum class CleanlinessStatus(val label: String, val colorHex: Long) {
    DIRTY("Dirty", 0xFFB54755),
    CLEANING("In Cleaning", 0xFFD99A28),
    INSPECTED("Inspected", 0xFF1D5AAB),
    READY("Ready", 0xFF23865B)
}

enum class ReservationStatus(val label: String, val colorHex: Long) {
    PENDING("Pending", 0xFFD99A28),
    CONFIRMED("Confirmed", 0xFF164E45),
    CHECKED_IN("Checked In", 0xFF23865B),
    CHECKED_OUT("Checked Out", 0xFF697586),
    CANCELLED("Cancelled", 0xFFB54755)
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer")
}

enum class PaymentStatus(val label: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    REFUNDED("Refunded")
}

enum class TaskPriority(val label: String, val colorHex: Long) {
    LOW("Low", 0xFF697586),
    STANDARD("Standard", 0xFF1D5AAB),
    HIGH("High", 0xFFD99A28),
    URGENT("Urgent VIP", 0xFFB54755)
}

enum class TaskStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    INSPECTED("Inspected"),
    COMPLETED("Completed")
}

enum class MaintenanceSeverity(val label: String, val colorHex: Long) {
    LOW("Low", 0xFF697586),
    MEDIUM("Medium", 0xFFD99A28),
    HIGH("High", 0xFFE05638),
    CRITICAL("Critical", 0xFFB54755)
}

enum class MaintenanceStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CANCELLED("Cancelled")
}

enum class StockMovementType(val label: String) {
    PURCHASE("Restock / In"),
    CONSUMPTION("Used / Out"),
    ADJUSTMENT("Audit Adjustment")
}

enum class StaffShift(val label: String, val hours: String) {
    MORNING("Morning", "07:00 - 15:00"),
    EVENING("Evening", "15:00 - 23:00"),
    NIGHT("Night", "23:00 - 07:00")
}

enum class StaffStatus(val label: String) {
    ON_DUTY("On Duty"),
    OFF_DUTY("Off Duty"),
    ON_LEAVE("On Leave")
}
