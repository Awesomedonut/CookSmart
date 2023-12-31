/** "NotificationWorker.kt"
 *  Description: A worker class which builds a notification with the notification
 *               helper class
 *  ast Modified: November 29, 2023L
 * */
package com.example.cooksmart.infra.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.cooksmart.infra.helper.NotificationHelper

class NotificationWorker(context : Context, workerParams : WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        val title = "An ingredient is about to expire!"
        val message = "Open CookSmart to determine what to do with your food"
        notificationHelper.showNotification(title, message)

        return Result.success()
    }
}