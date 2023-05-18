package com.example.impactAlert.model


class NotificationSender(val data: Data?, val to:String){
    constructor():this(null,""){}
}