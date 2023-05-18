package com.example.traffic_accident_detection_using_accelerometer.sendNotificationPack


import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.traffic_accident_detection_using_accelerometer.R
import com.example.traffic_accident_detection_using_accelerometer.activity.Constant
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.traffic_accident_detection_using_accelerometer.model.Token


class MyFireBaseMessagingService : FirebaseMessagingService() {
    private lateinit var title: String
    private lateinit var message: String

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateToken()
    }
    private fun updateToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(
                    baseContext,
                    "Token Did not get Generated",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnCompleteListener
            }
            val refreshToken: String = task.result.toString()

            val token = Token(refreshToken)
            FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token)


        })
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        title = remoteMessage.data["Title"].toString()
        message = remoteMessage.data["Message"].toString()
        createNotification(title, message)
    }

    private fun createNotification(title: String?, body: String?) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?q=$body")
        )
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            Constant.notificationId,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT } else { PendingIntent.FLAG_UPDATE_CURRENT } // setting the mutability flag
        )

        val emailObject =
            "Mr. $title might have been in an accident. You are nearby to him, He needs your help. Tap to get his current location."
        val notification = NotificationCompat.Builder(this, Constant.channelId)
            .setContentTitle("Emergency Alert")
            .setContentText("Mr. $title needs your help")
            .setSmallIcon(R.drawable.crash)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(emailObject)
            )
            .build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        val notificationManger = NotificationManagerCompat.from(this)
        notificationManger.notify(Constant.notificationId, notification)


    }
}