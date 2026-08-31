package com.example.finconapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//estructura de la tabla de base de datos sqlite
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val amount: Double,
    val description: String? = null, //Opcional
    val type: String, //Gasto o ingreso
    val category: String, //Negocio, doméstico, Colección, Gastos diarios
    val subcategory: String? = null, //Como tipo de negocio por ejemplo (opcional)
    val date: Long
)