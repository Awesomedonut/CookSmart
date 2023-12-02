package com.example.cooksmart.infra.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.cooksmart.infra.services.helper.NotificationHelper

class NotifyService: Service(){

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var myBroadcastReceiver: MyBroadcastReceiver
    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null
    companion object{
        const val KEY_LOCATION = "tracking location key"
        const val MSG_LOCATION_VALUE = 0
        const val STOP_SERVICE_ACTION = "stop service action"
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        val title = "test title"
        val message = "test msg"
        notificationHelper.showNotification(title, message)
        myBinder = MyBinder()
        myBroadcastReceiver = MyBroadcastReceiver()
        val intentFilter = IntentFilter()
        //Only listen a stop service action boardcast
        intentFilter.addAction(STOP_SERVICE_ACTION)
        registerReceiver(myBroadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@NotifyService.msgHandler = msgHandler
        }
    }
    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks(){
        notificationHelper.cleanupTasks()
    }

    private fun broadcast(location : Location){
        try {
            if(msgHandler != null){
                val bundle = Bundle()
                bundle.putDoubleArray(
                    KEY_LOCATION,
                    doubleArrayOf(location.latitude, location.longitude, location.altitude))
                val message = msgHandler!!.obtainMessage()
                message.data = bundle
                message.what = MSG_LOCATION_VALUE
                msgHandler!!.sendMessage(message)
            }

        } catch (t: Throwable) {
            Log.d("NotifyService", "broadcast failed")
        }
    }
    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            notificationHelper.cleanupTasks()
            unregisterReceiver(myBroadcastReceiver)
        }
    }
}