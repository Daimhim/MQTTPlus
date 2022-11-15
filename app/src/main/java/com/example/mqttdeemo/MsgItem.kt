package com.example.mqttdeemo

data class MsgItem(
    val msg:String,
    val time:Long,
    val isMe:Boolean = false
)