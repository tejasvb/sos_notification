package com.example.traffic_accident_detection_using_accelerometer.sendNotificationPack

import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.example.traffic_accident_detection_using_accelerometer.model.Token


class MyFirebaseIdService:FirebaseMessagingService(){
    override fun onNewToken(s:String){
        super.onNewToken(s)
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

}