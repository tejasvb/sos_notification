package com.example.traffic_accident_detection_using_accelerometer

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.vaibhavmojidra.demokotlin.SendNotificationPack.Token


class MyFirebaseIdService:FirebaseMessagingService(){
    override fun onNewToken(s:String){
        super.onNewToken(s)
        Log.d("dss"," token --> $s")
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var refreshToken:String = FirebaseMessaging.getInstance().token.toString()
       // if(firebaseUser!=null){
            updateToken(refreshToken)
      //  }
    }
    private fun updateToken(refreshToken:String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        var token: Token = Token(refreshToken)


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(
                    baseContext,
                    "Token Did not get Generated",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnCompleteListener
            }

            // fetching the token

            var refreshToken: String = task.result.toString()

            var token: Token = Token(refreshToken)
            FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token)


        })
    }

}