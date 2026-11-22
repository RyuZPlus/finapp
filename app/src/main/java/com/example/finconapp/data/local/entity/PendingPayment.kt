package com.example.finconapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_payments",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["dueDate"]),
        Index(value = ["status"])
    ]
)
data class PendingPayment(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nombre del pago
    val name: String,

    // Categoría asociada
    val categoryId: Int,

    // Importe total que debe pagarse
    val amount: Double,

    // Cantidad que ya se ha pagado
    val paidAmount: Double = 0.0,

    // Fecha límite de pago
    val dueDate: Long,

    // Fecha y hora en que se notificará al usuario
    val reminderDate: Long? = null,

    // Estado del pago: PENDING, PAID o CANCELLED
    val status: String = "PENDING",

    // Descripción opcional
    val description: String? = null
)