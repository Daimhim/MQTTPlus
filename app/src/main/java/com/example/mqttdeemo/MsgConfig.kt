package com.example.mqttdeemo

data class MsgConfig(
    val topic: String,
    val qos: Int,
    val retained:Boolean
)