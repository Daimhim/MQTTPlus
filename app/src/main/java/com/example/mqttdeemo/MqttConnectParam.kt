package com.example.mqttdeemo

import android.content.Context

data class MqttConnectParam (
    var context: Context? = null,
    var serverUrl: String? = null,
    var id: String? = null,
    var user: String? = null,
    var pass: String? = null,
    var isCleanSession:Boolean = false, //清除缓存
    var keepAliveInterval:Int = 30, //心跳包发送间隔，单位：秒
)