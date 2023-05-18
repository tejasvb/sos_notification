package com.example.traffic_accident_detection_using_accelerometer.model

import com.example.traffic_accident_detection_using_accelerometer.model.Data


class NotificationSender(val data: Data?, val to:String){
    constructor():this(null,""){}
}