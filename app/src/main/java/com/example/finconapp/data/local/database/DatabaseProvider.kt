package com.example.finconapp.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    fun provide(context: Context): AppDatabase {
        //Genera la base de datos en el dispositivo como finance_db
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "finance_db"
        ).build()
    }
}