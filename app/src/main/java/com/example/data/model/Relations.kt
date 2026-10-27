package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class RoomWithDetails(
    @Embedded val room: RoomEntity,
    @Relation(
        parentColumn = "roomTypeId",
        entityColumn = "id"
    )
    val roomType: RoomTypeEntity?
)

data class ReservationWithDetails(
    @Embedded val reservation: ReservationEntity,
    @Relation(
        parentColumn = "guestId",
        entityColumn = "id"
    )
    val guest: GuestEntity?,
    @Relation(
        parentColumn = "roomId",
        entityColumn = "id"
    )
    val room: RoomEntity?,
    @Relation(
        parentColumn = "roomTypeId",
        entityColumn = "id"
    )
    val roomType: RoomTypeEntity?
)

data class GuestWithReservations(
    @Embedded val guest: GuestEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "guestId"
    )
    val reservations: List<ReservationEntity>
)
