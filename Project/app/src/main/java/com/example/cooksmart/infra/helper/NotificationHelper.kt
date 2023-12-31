 /** "NotificationHelper.kt"
 *   Description: Helper class which retrieves the notification manager and builds
 *                a notification
 *   Last Modified: November 29, 2023
 * */
package com.example.cooksmart.infra.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cooksmart.R
import com.example.cooksmart.ui.recipe.RecipeFragment

class NotificationHelper (private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "cooksmart_service_channel"
        private const val CHANNEL_NAME = "cooksmart Service"
        private const val NOTIFICATION_ID = 777
    }

    fun showNotification(title : String, message : String) {
        val intent = Intent(context, RecipeFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
        notificationBuilder.setSmallIcon(R.drawable.grocery_24px)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(message)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(true)
        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        with(notificationManager) {
            notify(NOTIFICATION_ID, notification)
        }
    }
    fun cleanupTasks(){
        notificationManager.cancel(NOTIFICATION_ID)
    }
}