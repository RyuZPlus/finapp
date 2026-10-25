package com.example.finconapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class SavingsGoal(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val categoryId: Int,

    val targetAmount: Double,

    val initialAmount: Double = 0.0,

    val deadline: Long? = null
)