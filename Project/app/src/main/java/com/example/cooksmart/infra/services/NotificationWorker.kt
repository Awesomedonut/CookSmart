package com.example.cooksmart.infra.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.cooksmart.infra.services.helper.NotificationHelper

class NotificationWorker(context : Context, workerParams : WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        val title = "test title"
        val message = "test msg"
        notificationHelper.showNotification(title, message)

        return Result.success()
    }
}