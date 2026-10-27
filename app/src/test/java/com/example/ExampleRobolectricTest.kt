package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.LuxoraDatabase
import com.example.data.model.*
import com.example.data.repository.LuxoraRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: LuxoraDatabase
    private lateinit var repository: LuxoraRepository
    private lateinit var context: Context

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LuxoraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        LuxoraDatabase.seedInitialData(db)
        repository = LuxoraRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("LUXORA", appName)
    }

    @Test
    fun testDatabaseSeedDataLoaded() = runBlocking {
        val config = db.configDao().getConfig()
        assertNotNull(config)
        assertEquals("LUXORA Grand Palace & Spa", config?.hotelName)

        val room = db.roomDao().getRoomByNumber("501")
        assertNotNull(room)
        assertEquals(RoomStatus.OCCUPIED, room?.status)

        val user = db.userDao().getUserByUsername("admin")
        assertNotNull(user)
        assertEquals(UserRole.SUPER_ADMIN, user?.role)
    }

    @Test
    fun testDoubleBookingPrevention() = runBlocking {
        // Room 1 (501) has existing reservation for 2026-10-25 to 2026-10-29
        val result = repository.createReservation(
            guestId = 2,
            roomId = 1,
            roomTypeId = 1,
            checkInDate = "2026-10-26",
            checkOutDate = "2026-10-28",
            nights = 2,
            adults = 2,
            children = 0,
            nightlyRate = 1850.0,
            specialRequests = "VIP treatment"
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already booked") == true)
    }

    @Test
    fun testCheckInAndKeyCardIssuance() = runBlocking {
        // Reservation #2 is for Lady Sofia Kensington, Room 3 (401), currently CONFIRMED
        val res = db.reservationDao().getReservationByRef("LX-2026-9042")
        assertNotNull(res)
        assertEquals(ReservationStatus.CONFIRMED, res?.status)
        assertFalse(res?.keyCardIssued == true)

        val checkInResult = repository.checkInGuest(res!!.id)
        assertTrue(checkInResult.isSuccess)

        val updatedRes = db.reservationDao().getReservationById(res.id)
        assertEquals(ReservationStatus.CHECKED_IN, updatedRes?.reservation?.status)
        assertTrue(updatedRes?.reservation?.keyCardIssued == true)

        val updatedRoom = db.roomDao().getRoomById(res.roomId)
        assertEquals(RoomStatus.OCCUPIED, updatedRoom?.status)
    }

    @Test
    fun testFolioChargeAndPaymentSettlement() = runBlocking {
        val res = db.reservationDao().getReservationByRef("LX-2026-9041")!!
        val initialBalance = res.balanceDue

        // Add Michelin dining folio charge
        repository.addFolioCharge(res.id, "Fine Dining", "Caviar Tasting", 300.0, 1)

        val updatedResAfterCharge = db.reservationDao().getReservationById(res.id)!!
        assertEquals(initialBalance + 300.0, updatedResAfterCharge.reservation.balanceDue, 0.01)

        // Record payment settlement
        val payResult = repository.recordPayment(res.id, updatedResAfterCharge.reservation.balanceDue, PaymentMethod.CREDIT_CARD, "Settled in full")
        assertTrue(payResult.isSuccess)

        val settledRes = db.reservationDao().getReservationById(res.id)!!
        assertEquals(0.0, settledRes.reservation.balanceDue, 0.01)
    }
}
