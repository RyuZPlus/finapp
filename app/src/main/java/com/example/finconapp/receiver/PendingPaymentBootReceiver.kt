package com.example.finconapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.utils.PendingPaymentReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PendingPaymentBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = DatabaseProvider
                    .provide(context.applicationContext)
                    .pendingPaymentDao()

                val payments = dao.getAllPendingPaymentsForBackup()

                val scheduler = PendingPaymentReminderScheduler(context)

                payments
                    .filter { it.status == "PENDING" }
                    .forEach { payment ->
                        scheduler.schedule(payment)
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
