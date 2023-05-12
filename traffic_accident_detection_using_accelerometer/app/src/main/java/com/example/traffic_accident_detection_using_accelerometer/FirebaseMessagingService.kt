package com.example.traffic_accident_detection_using_accelerometer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "FirebaseMessagingService"
    private val CHANNEL_ID = "i.apps.notifications"
    private val CHANNEL_NAME = "Test notification"
    val NOTIF_ID = 0
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("$tag token --> $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        try {

            if (remoteMessage.notification != null) {
           createNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
            } else {
            createNotification(remoteMessage.data["title"], remoteMessage.data["message"])
            }

        } catch (e: Exception) {
            println("$tag error -->${e.localizedMessage}")
        }
    }
   private fun createNotification(title:String?,body:String?){
       val intent = Intent(
           Intent.ACTION_VIEW,
           Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345")
       )
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIF_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
        )

        val emailObject = "Mr. $title might have been in an accident. You are nearby him He needs your help. Tap to get his current location.";
        val notif = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Emergency Alert")
            .setContentText("Mr. $title needs your help")
            .setSmallIcon(R.drawable.crash)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(emailObject))
            .build()
        notif.flags = notif.flags or Notification.FLAG_AUTO_CANCEL
        val notifManger = NotificationManagerCompat.from(this)
        notifManger.notify(NOTIF_ID,notif)


    }
}