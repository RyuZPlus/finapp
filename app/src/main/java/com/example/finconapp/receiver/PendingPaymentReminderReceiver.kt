
package com.example.finconapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.finconapp.MainActivity
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.utils.PendingPaymentReminderScheduler
import com.example.finconapp.utils.ReminderDateCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PendingPaymentReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val paymentId = intent.getIntExtra(
            PendingPaymentReminderScheduler.EXTRA_PAYMENT_ID,
            -1
        )

        val scheduledTime = intent.getLongExtra(
            PendingPaymentReminderScheduler.EXTRA_REMINDER_TIME,
            -1L
        )

        if (paymentId == -1 || scheduledTime == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = DatabaseProvider
                    .provide(context.applicationContext)
                    .pendingPaymentDao()

                val payment = dao.getPendingPaymentById(paymentId)

                if (payment == null || payment.status != "PENDING") {
                    return@launch
                }

                val currentReminderTime = when (payment.reminderType) {
                    "CUSTOM" -> payment.reminderDate
                    else -> ReminderDateCalculator.calculate(
                        payment.dueDate,
                        payment.reminderType
                    )
                }

                // Ignorar alarmas antiguas de pagos editados.
                if (currentReminderTime != scheduledTime) {
                    return@launch
                }

                showNotification(
                    context = context,
                    paymentId = payment.id,
                    paymentName = payment.name
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        paymentId: Int,
        paymentName: String
    ) {
        // Android 13 o superior requiere autorización.
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de pagos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de pagos pendientes"
            }

            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            paymentId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio de pago")
            .setContentText("Recuerda realizar el pago: $paymentName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Tienes un pago pendiente: $paymentName")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        manager.notify(paymentId, notification)
    }

    companion object {
        const val CHANNEL_ID = "pending_payment_reminders"
    }
}
