package com.vaibhavmojidra.demokotlin.SendNotificationPack


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
public interface APIService {
    @Headers(
            "Content-Type:application/json",
            "Authorization:key=AAAA_FOJe4A:APA91bG51WHpTAQjZ9CUFok7puZL1sRne-f753FRv9DN7yJEGtb6Ck_WopbDRVGsBbv7SfqNXGmia9FDaLwf2wU1QJy2q5zwCjfZZDbO2jh5oUfM9wucmT_zU0-YVVC4_Dhgujf7Y1-p" // Your server key refer to video for finding your server key
    )
    @POST("fcm/send")
    open fun sendNotifcation(@Body body: NotificationSender?): Call<MyResponse?>?
}