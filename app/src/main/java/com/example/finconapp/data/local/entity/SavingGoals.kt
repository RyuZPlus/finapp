package com.example.finconapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val categoryId: Int,

    val targetAmount: Double,

    val initialAmount: Double = 0.0,

    val deadline: Long? = null
)