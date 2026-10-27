package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        RoomTypeEntity::class,
        RoomEntity::class,
        GuestEntity::class,
        ReservationEntity::class,
        FolioItemEntity::class,
        PaymentEntity::class,
        InvoiceEntity::class,
        HousekeepingTaskEntity::class,
        MaintenanceRequestEntity::class,
        StaffEntity::class,
        InventoryItemEntity::class,
        ExpenseEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        HotelConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LuxoraDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun guestDao(): GuestDao
    abstract fun reservationDao(): ReservationDao
    abstract fun billingDao(): BillingDao
    abstract fun housekeepingDao(): HousekeepingDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun staffDao(): StaffDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: LuxoraDatabase? = null

        fun getDatabase(context: Context): LuxoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LuxoraDatabase::class.java,
                    "luxora_hotel.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedDataCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedDataCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(db: LuxoraDatabase) {
            // Seed Hotel Configuration
            db.configDao().insertOrUpdate(
                HotelConfigEntity(
                    id = 1,
                    hotelName = "LUXORA Grand Palace & Spa",
                    tagLine = "Unrivaled Luxury & Bespoke Hospitality",
                    address = "88 Grand Horizon Boulevard, Waterfront District",
                    phone = "+1 (800) 589-6721",
                    email = "concierge@luxorahotel.com",
                    currencySymbol = "$",
                    currencyCode = "USD",
                    taxRatePercent = 12.0,
                    serviceChargePercent = 5.0,
                    checkInTime = "15:00",
                    checkOutTime = "12:00"
                )
            )

            // Seed Users
            val users = listOf(
                UserEntity(
                    id = 1,
                    username = "admin",
                    passwordHash = "admin123",
                    fullName = "Lord Harrison Vance",
                    email = "director@luxorahotel.com",
                    role = UserRole.SUPER_ADMIN
                ),
                UserEntity(
                    id = 2,
                    username = "manager",
                    passwordHash = "manager123",
                    fullName = "Elena Rostova",
                    email = "general.manager@luxorahotel.com",
                    role = UserRole.HOTEL_MANAGER
                ),
                UserEntity(
                    id = 3,
                    username = "reception",
                    passwordHash = "reception123",
                    fullName = "Julian Sterling",
                    email = "frontdesk@luxorahotel.com",
                    role = UserRole.RECEPTIONIST
                ),
                UserEntity(
                    id = 4,
                    username = "accountant",
                    passwordHash = "account123",
                    fullName = "Marcus Aurelius Chen",
                    email = "finance@luxorahotel.com",
                    role = UserRole.ACCOUNTANT
                ),
                UserEntity(
                    id = 5,
                    username = "housekeeping",
                    passwordHash = "clean123",
                    fullName = "Camilla Rossi",
                    email = "housekeeping@luxorahotel.com",
                    role = UserRole.HOUSEKEEPING
                )
            )
            db.userDao().insertAll(users)

            // Seed Room Types
            val roomTypes = listOf(
                RoomTypeEntity(
                    id = 1,
                    code = "PENTHOUSE",
                    name = "Royal Penthouse Suite",
                    description = "Panoramic ocean & skyline views, private infinity terrace, bespoke 24-hr butler service, dining salon & marble jacuzzi.",
                    basePrice = 1850.0,
                    capacityAdults = 4,
                    capacityChildren = 2,
                    bedType = "Emperor King + Twin Stateroom",
                    sizeSqMeters = 240,
                    amenities = "Private Terrace, Marble Jacuzzi, Butler Service, Chilled Champagne Bar, Bang & Olufsen Audio"
                ),
                RoomTypeEntity(
                    id = 2,
                    code = "PRESIDENTIAL",
                    name = "Presidential Diplomatic Suite",
                    description = "Executive grandeur with grand reception foyer, boardroom table, private sauna, and sommelier selection.",
                    basePrice = 1250.0,
                    capacityAdults = 3,
                    capacityChildren = 1,
                    bedType = "Grand California King",
                    sizeSqMeters = 160,
                    amenities = "Private Sauna, Wine Cellar, Executive Boardroom, Walk-in Dressing Salon, City Skyline View"
                ),
                RoomTypeEntity(
                    id = 3,
                    code = "EXEC_OCEAN",
                    name = "Executive Ocean Suite",
                    description = "Sweeping coastal balcony, separate master salon, freestanding soaking tub and artisan cocktail cabinet.",
                    basePrice = 750.0,
                    capacityAdults = 2,
                    capacityChildren = 1,
                    bedType = "Plush Super King",
                    sizeSqMeters = 95,
                    amenities = "Private Ocean Balcony, Freestanding Tub, Nespresso Bar, Walk-in Rain Shower, Pillow Menu"
                ),
                RoomTypeEntity(
                    id = 4,
                    code = "DELUXE_KING",
                    name = "Grand Deluxe King",
                    description = "Refined sanctuary featuring handcrafted Italian linens, bespoke writing desk, and floor-to-ceiling soundproof glass.",
                    basePrice = 480.0,
                    capacityAdults = 2,
                    capacityChildren = 1,
                    bedType = "Custom Plush King",
                    sizeSqMeters = 65,
                    amenities = "Marble Bathroom, Smart Climate, Artisanal Minibar, High-Speed Wi-Fi, Turndown Chocolates"
                ),
                RoomTypeEntity(
                    id = 5,
                    code = "HERITAGE_TWIN",
                    name = "Heritage Twin Deluxe",
                    description = "Twin stateroom beds with bespoke mahogany detailing, library nook, and garden courtyard vista.",
                    basePrice = 420.0,
                    capacityAdults = 2,
                    capacityChildren = 0,
                    bedType = "Two Double Plush Beds",
                    sizeSqMeters = 55,
                    amenities = "Courtyard View, Twin Vanity, Botanical Toiletries, Reading Nook, Valet Pressing"
                )
            )
            db.roomDao().insertRoomTypes(roomTypes)

            // Seed Rooms
            val rooms = listOf(
                RoomEntity(1, "501", 5, 1, RoomStatus.OCCUPIED, CleanlinessStatus.INSPECTED, "Duke Wellington", 1),
                RoomEntity(2, "502", 5, 1, RoomStatus.AVAILABLE, CleanlinessStatus.READY, null, null),
                RoomEntity(3, "401", 4, 2, RoomStatus.RESERVED, CleanlinessStatus.READY, "Lady Sofia Kensington", 2),
                RoomEntity(4, "402", 4, 2, RoomStatus.AVAILABLE, CleanlinessStatus.READY, null, null),
                RoomEntity(5, "301", 3, 3, RoomStatus.OCCUPIED, CleanlinessStatus.INSPECTED, "Dr. Alistair Finch", 3),
                RoomEntity(6, "302", 3, 3, RoomStatus.AVAILABLE, CleanlinessStatus.READY, null, null),
                RoomEntity(7, "303", 3, 3, RoomStatus.AVAILABLE, CleanlinessStatus.DIRTY, null, null),
                RoomEntity(8, "201", 2, 4, RoomStatus.OCCUPIED, CleanlinessStatus.INSPECTED, "Baroness Claire Dupont", 4),
                RoomEntity(9, "202", 2, 4, RoomStatus.AVAILABLE, CleanlinessStatus.CLEANING, null, null),
                RoomEntity(10, "203", 2, 4, RoomStatus.AVAILABLE, CleanlinessStatus.READY, null, null),
                RoomEntity(11, "204", 2, 4, RoomStatus.MAINTENANCE, CleanlinessStatus.DIRTY, null, null, "HVAC climate calibration"),
                RoomEntity(12, "101", 1, 5, RoomStatus.AVAILABLE, CleanlinessStatus.READY, null, null),
                RoomEntity(13, "102", 1, 5, RoomStatus.OCCUPIED, CleanlinessStatus.INSPECTED, "Captain Arthur Hastings", 5),
                RoomEntity(14, "103", 1, 5, RoomStatus.AVAILABLE, CleanlinessStatus.READY, null, null)
            )
            db.roomDao().insertRooms(rooms)

            // Seed Guests
            val guests = listOf(
                GuestEntity(
                    id = 1,
                    firstName = "Duke",
                    lastName = "Wellington",
                    email = "duke.wellington@mayfair.co.uk",
                    phone = "+44 20 7946 0912",
                    idOrPassport = "UK-PASS-982144",
                    country = "United Kingdom",
                    isVip = true,
                    preferences = "High floor, Chilled Krug Champagne at 18:00, Extra hypoallergenic pillows",
                    notes = "Distinguished dignitary. Ensure GM greeting upon arrival.",
                    totalStays = 6,
                    lifetimeSpend = 24800.0
                ),
                GuestEntity(
                    id = 2,
                    firstName = "Sofia",
                    lastName = "Kensington",
                    email = "sofia.k@kensingtonglobal.com",
                    phone = "+1 212 555 0199",
                    idOrPassport = "US-PASS-314890",
                    country = "United States",
                    isVip = true,
                    preferences = "Allergic to shellfish. Prefers sparkling San Pellegrino and fresh white orchids.",
                    notes = "Executive VIP Corporate Client.",
                    totalStays = 4,
                    lifetimeSpend = 14200.0
                ),
                GuestEntity(
                    id = 3,
                    firstName = "Alistair",
                    lastName = "Finch",
                    email = "dr.finch@oxfordmed.ac.uk",
                    phone = "+44 1865 496011",
                    idOrPassport = "UK-PASS-761209",
                    country = "United Kingdom",
                    isVip = false,
                    preferences = "Quiet room, Financial Times physical newspaper at 06:30",
                    notes = "Attending International Surgical Symposium.",
                    totalStays = 2,
                    lifetimeSpend = 3100.0
                ),
                GuestEntity(
                    id = 4,
                    firstName = "Claire",
                    lastName = "Dupont",
                    email = "c.dupont@atelierdupont.fr",
                    phone = "+33 1 42 68 55 00",
                    idOrPassport = "FR-PASS-448102",
                    country = "France",
                    isVip = true,
                    preferences = "Silk hangers, late checkout 14:00 requested",
                    notes = "Renowned jewelry designer. Guest of Honor.",
                    totalStays = 8,
                    lifetimeSpend = 38900.0
                ),
                GuestEntity(
                    id = 5,
                    firstName = "Arthur",
                    lastName = "Hastings",
                    email = "hastings.a@admiralty.org",
                    phone = "+61 2 9331 4400",
                    idOrPassport = "AU-PASS-552918",
                    country = "Australia",
                    isVip = false,
                    preferences = "Early morning ocean view running map",
                    notes = "Maritime consultant.",
                    totalStays = 1,
                    lifetimeSpend = 1680.0
                )
            )
            db.guestDao().insertGuests(guests)

            // Seed Reservations
            val reservations = listOf(
                ReservationEntity(
                    id = 1,
                    bookingReference = "LX-2026-9041",
                    guestId = 1,
                    roomId = 1,
                    roomTypeId = 1,
                    checkInDate = "2026-10-25",
                    checkOutDate = "2026-10-29",
                    nights = 4,
                    adults = 2,
                    children = 0,
                    nightlyRate = 1850.0,
                    taxesAmount = 888.0,
                    discountAmount = 200.0,
                    totalAmount = 8088.0,
                    depositPaid = 4000.0,
                    balanceDue = 4088.0,
                    status = ReservationStatus.CHECKED_IN,
                    specialRequests = "Private helicopter transfer confirmation",
                    keyCardIssued = true
                ),
                ReservationEntity(
                    id = 2,
                    bookingReference = "LX-2026-9042",
                    guestId = 2,
                    roomId = 3,
                    roomTypeId = 2,
                    checkInDate = "2026-10-26",
                    checkOutDate = "2026-10-30",
                    nights = 4,
                    adults = 2,
                    children = 1,
                    nightlyRate = 1250.0,
                    taxesAmount = 600.0,
                    discountAmount = 0.0,
                    totalAmount = 5600.0,
                    depositPaid = 2000.0,
                    balanceDue = 3600.0,
                    status = ReservationStatus.CONFIRMED,
                    specialRequests = "Boardroom booking for 2 hours on arrival",
                    keyCardIssued = false
                ),
                ReservationEntity(
                    id = 3,
                    bookingReference = "LX-2026-9043",
                    guestId = 3,
                    roomId = 5,
                    roomTypeId = 3,
                    checkInDate = "2026-10-24",
                    checkOutDate = "2026-10-27",
                    nights = 3,
                    adults = 1,
                    children = 0,
                    nightlyRate = 750.0,
                    taxesAmount = 270.0,
                    discountAmount = 50.0,
                    totalAmount = 2470.0,
                    depositPaid = 2470.0,
                    balanceDue = 0.0,
                    status = ReservationStatus.CHECKED_IN,
                    specialRequests = "High floor quiet corner",
                    keyCardIssued = true
                ),
                ReservationEntity(
                    id = 4,
                    bookingReference = "LX-2026-9044",
                    guestId = 4,
                    roomId = 8,
                    roomTypeId = 4,
                    checkInDate = "2026-10-23",
                    checkOutDate = "2026-10-26",
                    nights = 3,
                    adults = 2,
                    children = 0,
                    nightlyRate = 480.0,
                    taxesAmount = 172.8,
                    discountAmount = 0.0,
                    totalAmount = 1612.8,
                    depositPaid = 1612.8,
                    balanceDue = 0.0,
                    status = ReservationStatus.CHECKED_IN,
                    specialRequests = "Late departure 14:00 requested",
                    keyCardIssued = true
                ),
                ReservationEntity(
                    id = 5,
                    bookingReference = "LX-2026-9045",
                    guestId = 5,
                    roomId = 13,
                    roomTypeId = 5,
                    checkInDate = "2026-10-25",
                    checkOutDate = "2026-10-28",
                    nights = 3,
                    adults = 2,
                    children = 0,
                    nightlyRate = 420.0,
                    taxesAmount = 151.2,
                    discountAmount = 0.0,
                    totalAmount = 1411.2,
                    depositPaid = 1000.0,
                    balanceDue = 411.2,
                    status = ReservationStatus.CHECKED_IN,
                    specialRequests = "Twin bedding confirmed",
                    keyCardIssued = true
                )
            )
            db.reservationDao().insertReservations(reservations)

            // Seed Folio Items
            val folioItems = listOf(
                FolioItemEntity(1, 1, 1, "Room Stay", "Royal Penthouse (4 Nights @ $1,850)", 1850.0, 4, 7400.0, "2026-10-25"),
                FolioItemEntity(2, 1, 1, "Fine Dining", "L'Etoile Michelin Degustation Dinner", 420.0, 1, 420.0, "2026-10-25"),
                FolioItemEntity(3, 1, 1, "Spa & Wellness", "Imperial Gold Leaf Signature Massage", 350.0, 1, 350.0, "2026-10-26"),
                FolioItemEntity(4, 3, 3, "Room Stay", "Executive Ocean Suite (3 Nights @ $750)", 750.0, 3, 2250.0, "2026-10-24"),
                FolioItemEntity(5, 3, 3, "Room Service", "Artisan Smoked Salmon & Champagne Breakfast", 85.0, 1, 85.0, "2026-10-25"),
                FolioItemEntity(6, 4, 4, "Room Stay", "Grand Deluxe King (3 Nights @ $480)", 480.0, 3, 1440.0, "2026-10-23"),
                FolioItemEntity(7, 4, 4, "Laundry", "Same-Day Haute Couture Garment Pressing", 65.0, 1, 65.0, "2026-10-24")
            )
            db.billingDao().insertFolioItems(folioItems)

            // Seed Payments
            val payments = listOf(
                PaymentEntity(1, 1, 1, 4000.0, PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED, "TXN-CC-89412", "Deposit received", "2026-10-25"),
                PaymentEntity(2, 2, 2, 2000.0, PaymentMethod.BANK_TRANSFER, PaymentStatus.COMPLETED, "TXN-BT-91024", "Pre-arrival advance", "2026-10-24"),
                PaymentEntity(3, 3, 3, 2470.0, PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED, "TXN-CC-77149", "Full settlement on check-in", "2026-10-24"),
                PaymentEntity(4, 4, 4, 1612.8, PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED, "TXN-CC-66412", "Paid in full", "2026-10-23"),
                PaymentEntity(5, 5, 5, 1000.0, PaymentMethod.CASH, PaymentStatus.COMPLETED, "TXN-CSH-103", "Cash deposit at front desk", "2026-10-25")
            )
            db.billingDao().insertPayments(payments)

            // Seed Invoices
            val invoices = listOf(
                InvoiceEntity(1, "INV-2026-0081", 4, 4, "Claire Dupont", "201", "2026-10-26", 1440.0, 172.8, 0.0, 1612.8, 1612.8, 0.0, true),
                InvoiceEntity(2, "INV-2026-0082", 3, 3, "Alistair Finch", "301", "2026-10-26", 2250.0, 270.0, 50.0, 2470.0, 2470.0, 0.0, true)
            )
            for (inv in invoices) {
                db.billingDao().insertInvoice(inv)
            }

            // Seed Housekeeping Tasks
            val tasks = listOf(
                HousekeepingTaskEntity(1, 7, "303", "Turnover Clean", TaskPriority.HIGH, TaskStatus.PENDING, "Camilla Rossi", "Full sanitization, fresh Egyptian linens, replace Bvlgari toiletries", "Guest departed 11:30"),
                HousekeepingTaskEntity(2, 9, "202", "Daily Service", TaskPriority.STANDARD, TaskStatus.IN_PROGRESS, "Marco Silva", "Bed dressing, towel refresh, balcony glass polishing", "Do Not Disturb cleared at 10:00"),
                HousekeepingTaskEntity(3, 1, "501", "VIP Turndown", TaskPriority.URGENT, TaskStatus.INSPECTED, "Camilla Rossi", "Scented linen spray, silver tray chocolates, slippers bedside", "Completed by Senior Housekeeper"),
                HousekeepingTaskEntity(4, 11, "204", "Deep Clean", TaskPriority.LOW, TaskStatus.PENDING, "Unassigned", "Carpet steam and curtain sanitization", "Post maintenance overhaul")
            )
            db.housekeepingDao().insertTasks(tasks)

            // Seed Maintenance Requests
            val maintenance = listOf(
                MaintenanceRequestEntity(1, 11, "204", "Thermostat display erratic", "Digital wall thermostat flickering and temperature offset by 3 degrees", "HVAC", MaintenanceSeverity.HIGH, MaintenanceStatus.IN_PROGRESS, "Julian Sterling", "Dimitri (Chief Engineer)", "2026-10-25", null, "New relay sensor ordered"),
                MaintenanceRequestEntity(2, 6, "302", "Balcony door seal lubrication", "Slight friction sound when opening sliding door", "Furniture", MaintenanceSeverity.LOW, MaintenanceStatus.PENDING, "Camilla Rossi", "Engineering Team", "2026-10-26", null, ""),
                MaintenanceRequestEntity(3, 1, "501", "Jacuzzi jet pressure test", "Routine pre-arrival verification", "Plumbing", MaintenanceSeverity.MEDIUM, MaintenanceStatus.RESOLVED, "Elena Rostova", "Dimitri", "2026-10-24", "2026-10-24", "Pressure optimal at 4.2 bar")
            )
            db.maintenanceDao().insertRequests(maintenance)

            // Seed Staff
            val staff = listOf(
                StaffEntity(1, "Lord Harrison Vance", "director@luxorahotel.com", "+1 800 589 6701", "Managing Director", "Management", StaffShift.MORNING, StaffStatus.ON_DUTY, "2022-01-15"),
                StaffEntity(2, "Elena Rostova", "elena@luxorahotel.com", "+1 800 589 6702", "General Manager", "Management", StaffShift.MORNING, StaffStatus.ON_DUTY, "2022-06-01"),
                StaffEntity(3, "Julian Sterling", "julian@luxorahotel.com", "+1 800 589 6703", "Head Concierge & Front Desk", "Front Office", StaffShift.MORNING, StaffStatus.ON_DUTY, "2023-03-10"),
                StaffEntity(4, "Amara Thorne", "amara@luxorahotel.com", "+1 800 589 6704", "Night Auditor & Receptionist", "Front Office", StaffShift.NIGHT, StaffStatus.OFF_DUTY, "2023-08-20"),
                StaffEntity(5, "Camilla Rossi", "camilla@luxorahotel.com", "+1 800 589 6705", "Executive Housekeeper", "Housekeeping", StaffShift.MORNING, StaffStatus.ON_DUTY, "2022-11-05"),
                StaffEntity(6, "Marco Silva", "marco@luxorahotel.com", "+1 800 589 6706", "Senior Floor Attendant", "Housekeeping", StaffShift.EVENING, StaffStatus.ON_DUTY, "2024-02-14"),
                StaffEntity(7, "Marcus Aurelius Chen", "marcus@luxorahotel.com", "+1 800 589 6707", "Financial Controller", "Finance", StaffShift.MORNING, StaffStatus.ON_DUTY, "2023-01-20"),
                StaffEntity(8, "Dimitri Volkov", "dimitri@luxorahotel.com", "+1 800 589 6708", "Chief Engineer", "Engineering", StaffShift.MORNING, StaffStatus.ON_DUTY, "2022-09-12")
            )
            db.staffDao().insertAllStaff(staff)

            // Seed Inventory Items
            val inventory = listOf(
                InventoryItemEntity(1, "Hermès Eau d'Orange Verte Amenities (Sets)", "Toiletries", "TOIL-HRM-01", "sets", 18, 25, 45.0, "Hermès Parfums Paris", "2026-10-15"),
                InventoryItemEntity(2, "1,000-Thread Count Egyptian Cotton Sheets", "Linen", "LIN-EGY-01", "sets", 42, 20, 220.0, "Rivolta Carmignani", "2026-10-10"),
                InventoryItemEntity(3, "Dom Pérignon Vintage 2015 Champagne", "F&B", "FB-DOM-15", "bottles", 14, 15, 240.0, "Moët Hennessy", "2026-10-18"),
                InventoryItemEntity(4, "Nespresso Master Origin Espresso Pods", "F&B", "FB-NES-99", "boxes", 85, 30, 12.5, "Nespresso Professional", "2026-10-22"),
                InventoryItemEntity(5, "Bespoke White Orchid Plants", "Decor", "DEC-ORC-04", "pcs", 8, 10, 65.0, "Botanical Florists", "2026-10-20"),
                InventoryItemEntity(6, "Plush Terry Velour Bathrobes", "Linen", "LIN-ROB-02", "pcs", 30, 25, 85.0, "Frette Luxury Linens", "2026-10-12")
            )
            db.inventoryDao().insertItems(inventory)

            // Seed Expenses
            val expenses = listOf(
                ExpenseEntity(1, "F&B Procurement", "Grand Cru Vintage wines & artisan cheeses", 4850.0, PaymentMethod.BANK_TRANSFER, "2026-10-24", "Caviar & Wine Imports", "Elena Rostova"),
                ExpenseEntity(2, "Housekeeping Supplies", "Replenishment of Hermès botanical sets", 1950.0, PaymentMethod.BANK_TRANSFER, "2026-10-22", "LVMH Hospitality", "Lord Harrison Vance"),
                ExpenseEntity(3, "Utilities & Power", "Central chilled water air conditioning & solar grid", 3200.0, PaymentMethod.BANK_TRANSFER, "2026-10-20", "Metropolitan Grid", "Marcus Aurelius Chen"),
                ExpenseEntity(4, "Maintenance & Repairs", "HVAC high-precision digital thermostats", 890.0, PaymentMethod.CREDIT_CARD, "2026-10-25", "Honeywell Industrial", "Dimitri Volkov")
            )
            db.expenseDao().insertExpenses(expenses)

            // Seed Notifications
            val notifications = listOf(
                NotificationEntity(1, "VIP Guest Arrival Today", "Lady Sofia Kensington arrives at 15:00 in Presidential Suite 401. Ensure white orchids and welcome card.", "VIP", false),
                NotificationEntity(2, "Inventory Low Stock Alert", "Hermès Eau d'Orange Verte Amenities is at 18 sets (Minimum threshold: 25). Purchase order advised.", "Inventory", false),
                NotificationEntity(3, "Maintenance Ticket Assigned", "Room 204 HVAC thermostat calibration is assigned to Dimitri Volkov.", "Maintenance", false),
                NotificationEntity(4, "Outstanding Guest Balance", "Duke Wellington folio balance is $4,088.00 in Penthouse 501.", "Payment", true),
                NotificationEntity(5, "Turnover Clean Required", "Room 303 requires turnover cleaning following guest departure.", "Departure", false)
            )
            db.notificationDao().insertNotifications(notifications)

            // Seed Audit Logs
            val auditLogs = listOf(
                AuditLogEntity(1, "SYSTEM_INITIALIZED", "Luxora Hotel Management System initialized with 5-star baseline catalog", "System", System.currentTimeMillis() - 86400000),
                AuditLogEntity(2, "RESERVATION_CREATED", "Booking LX-2026-9041 created for Duke Wellington in Suite 501", "Julian Sterling", System.currentTimeMillis() - 43200000),
                AuditLogEntity(3, "CHECK_IN_PROCESSED", "Guest checked in to Room 501, key card #KC-501 issued", "Julian Sterling", System.currentTimeMillis() - 36000000)
            )
            for (log in auditLogs) {
                db.auditLogDao().insertAuditLog(log)
            }
        }
    }
}
