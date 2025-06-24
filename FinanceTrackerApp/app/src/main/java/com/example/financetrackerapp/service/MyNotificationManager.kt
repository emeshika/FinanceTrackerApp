package com.example.financetrackerapp.service


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.financetrackerapp.MainActivity
import com.example.financetrackerapp.R


//use Singleton Pattern in here
class MyNotificationManager private constructor(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "budget_alerts"
        private const val CHANNEL_NAME = "Budget Alerts"
        private const val NOTIFICATION_ID = 1
        private var instance: MyNotificationManager? = null

        //return singleton instance
        fun getInstance(context: Context): MyNotificationManager {
            return instance ?: synchronized(this) {
                instance ?: MyNotificationManager(context).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannel()
    }

    //set channel for Android 8+
    private fun createNotificationChannel() {
        //createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Budget alert notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    //notifications trigger
    fun sendBudgetAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Add runtime permission request if needed (API 33+)
            Toast.makeText(context, "Please enable notification permission", Toast.LENGTH_LONG).show()
        }

        //when we click the notification redirect to the main activity.
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build Notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Limit Reached")
            .setContentText("You've reached your monthly budget limit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show Notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
