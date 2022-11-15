package com.example.mqttdeemo

data class MessageBody(
    val msg: String,
    val topic: String,
    /**
     *     QoS0	无离线消息，在线消息只尝试推一次。	无离线消息，在线消息只尝试推一次。
     *    QoS1	无离线消息，在线消息保证可达。	有离线消息，所有消息保证可达。
     *    QoS2	无离线消息，在线消息保证只推一次。	暂不支持。
     */
    val qos: Int,
    /**
     * 此消息是否应由服务器保留
     */
    val retained: Boolean,
)