package com.example.finconapp.utils

import java.util.Calendar
import java.util.TimeZone

object ReminderDateCalculator {

    fun calculate(
        dueDate: Long,
        reminderType: String
    ): Long? {

        val daysBefore = when (reminderType) {
            "DUE_DATE" -> 0
            "ONE_DAY" -> 1
            "THREE_DAYS" -> 3
            "ONE_WEEK" -> 7
            else -> return null
        }

        // La fecha de vencimiento se interpreta como fecha UTC.
        val utcCalendar = Calendar.getInstance(
            TimeZone.getTimeZone("UTC")
        ).apply {
            timeInMillis = dueDate
        }

        val year = utcCalendar.get(Calendar.YEAR)
        val month = utcCalendar.get(Calendar.MONTH)
        val day = utcCalendar.get(Calendar.DAY_OF_MONTH)

        // El recordatorio se construye en horario local.
        return Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            add(Calendar.DAY_OF_MONTH, -daysBefore)
        }.timeInMillis
    }
}