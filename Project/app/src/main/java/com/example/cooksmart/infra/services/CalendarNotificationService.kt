package com.example.cooksmart.infra.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.cooksmart.Constants.CALENDAR_CHANNEL_ID
import com.example.cooksmart.Constants.NOTIFICATION_ID
import com.example.cooksmart.MainActivity
import com.example.cooksmart.R
import com.example.cooksmart.ui.calendar.CalendarFragment

class CalendarNotificationService :  Service(){
    // Binding-related object
    private lateinit var myBinder : MyBinder
    private var isBind = false

    private var msgHandler : Handler? = null

    private lateinit var notificationManager : NotificationManager

    override fun onCreate() {
        super.onCreate()
        myBinder = MyBinder()
        msgHandler = null
        createNotification()
    }

    private fun createNotification() {
        if(Build.VERSION.SDK_INT < 26){
            return
        }
        val builder = NotificationCompat.Builder(this, CALENDAR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Test notif title")
            .setContentText("Test notif text")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notif = builder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CALENDAR_CHANNEL_ID,
                "CookSmart Calendar",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notif)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    inner class MyBinder : Binder(){
        fun setmsgHandler(msgHandler : Handler){
            this@CalendarNotificationService.msgHandler = msgHandler
        }
    }
}