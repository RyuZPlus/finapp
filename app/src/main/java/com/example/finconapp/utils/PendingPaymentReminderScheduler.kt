
package com.example.finconapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.finconapp.data.local.entity.PendingPayment
import com.example.finconapp.receiver.PendingPaymentReminderReceiver

class PendingPaymentReminderScheduler(
    private val context: Context
) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(payment: PendingPayment) {
        cancel(payment.id)

        if (payment.status != "PENDING") return

        val reminderTime = when (payment.reminderType) {
            "CUSTOM" -> payment.reminderDate
            else -> ReminderDateCalculator.calculate(
                payment.dueDate,
                payment.reminderType
            )
        } ?: return

        // No programar recordatorios que ya pasaron.
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(
            context,
            PendingPaymentReminderReceiver::class.java
        ).apply {
            action = ACTION_PENDING_PAYMENT_REMINDER
            putExtra(EXTRA_PAYMENT_ID, payment.id)
            putExtra(EXTRA_REMINDER_TIME, reminderTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            payment.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )
    }

    fun cancel(paymentId: Int) {
        val intent = Intent(
            context,
            PendingPaymentReminderReceiver::class.java
        ).apply {
            action = ACTION_PENDING_PAYMENT_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            paymentId,
            intent,
            PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    companion object {
        const val ACTION_PENDING_PAYMENT_REMINDER =
            "com.example.finconapp.PENDING_PAYMENT_REMINDER"

        const val EXTRA_PAYMENT_ID = "payment_id"
        const val EXTRA_REMINDER_TIME = "reminder_time"
    }
}
