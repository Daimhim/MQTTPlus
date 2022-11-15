package com.example.mqttdeemo

data class ServerConfig (
    val serverUrl:String = "tcp://broker-cn.emqx.io:1883",
    val mqttUserName:String = "",
    val mqttPassword:String = "",
    val mqttId:String = "0",
    val isCleanSession:Boolean = false, //清除缓存
    val keepAliveInterval:Int = 30, //心跳包发送间隔，单位：秒
)