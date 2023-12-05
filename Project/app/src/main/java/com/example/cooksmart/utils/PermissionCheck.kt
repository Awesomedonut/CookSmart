/** "PermissionCheck.kt"
 *  Description: A utility class which checks for permissions upon application launch
 *  Last Modified: December 4, 2023
 */
package com.example.cooksmart.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val reqCode : Int = 0
object PermissionCheck {
    /** "checkPermissions"
     *  Description: Checks for user permissions utilized in the application
     * */
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.RECORD_AUDIO), reqCode)
        }
    }

    /** "checkNotificationPermission"
     *  Description: Returns the result of checking for notification permissions. Used
     *               so application knows when to use Toast messages to notify user about
     *               missing permissions
     * */
    fun checkNotificationPermission(activity : Activity?) : Boolean{
        if(ContextCompat.checkSelfPermission(activity!!, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }
}