package com.example.traffic_accident_detection_using_accelerometer


import android.R
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.traffic_accident_detection_using_accelerometer.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFireBaseMessagingService : FirebaseMessagingService() {
    private lateinit var title: String;
    private lateinit var message: String;
    private val CHANNEL_ID = "i.apps.notifications"
    private val CHANNEL_NAME = "Test notification"
    val NOTIF_ID = 0
    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        title = remoteMessage.getData().get("Title").toString()
        message = remoteMessage.getData().get("Message").toString()
        createNotification(title, message)
    }

    private fun createNotification(title: String?, body: String?) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?q=$body")
        )
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIF_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
        )

        val emailObject =
            "Mr. $title might have been in an accident. You are around 1 Km away from him, He needs your help. Tap to get his current location.";
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Emergency Alert")
            .setContentText("Mr. $title needs your help")
            .setSmallIcon(com.example.traffic_accident_detection_using_accelerometer.R.drawable.crash)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(emailObject)
            )
            .build()
        notif.flags = notif.flags or Notification.FLAG_AUTO_CANCEL
        val notifManger = NotificationManagerCompat.from(this)
        notifManger.notify(NOTIF_ID, notif)


    }
}