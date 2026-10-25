package com.example.finconapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
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
data class Transaction(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val amount: Double,
    val description: String? = null,
    val type: String,
    val categoryId: Int,
    val subcategory: String? = null,
    val date: Long
)