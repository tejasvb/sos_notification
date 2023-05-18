package com.example.traffic_accident_detection_using_accelerometer.sendNotificationPack


import com.example.traffic_accident_detection_using_accelerometer.model.NotificationSender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
interface APIService {
    @Headers(
            "Content-Type:application/json",
            "Authorization:key=add_Token" // Your server
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: NotificationSender?): Call<MyResponse?>?
}