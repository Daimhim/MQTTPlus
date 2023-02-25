package com.example.mqttdeemo

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import timber.multiplatform.log.Timber


class Mqtt : MqttCallback,IMqttActionListener{
    companion object{
        private var mMqttServer: Mqtt? = null
        fun getInstance():Mqtt{
            if (mMqttServer == null) {
                synchronized(Mqtt::class.java) {
                    if (mMqttServer == null) {
                        mMqttServer = Mqtt()
                    }
                }
            }
            return mMqttServer!!
        }
    }

    private var mClient: MqttAndroidClient? = null
    private var isConninging = false
    private var callback: MessageCallback? = null

    fun tryConnect(param: MqttConnectParam, callback: MessageCallback) {
        this.callback = callback
        if (!isConnected()) {
            Timber.i("连接MqttServer -------start")
            connectMqttServer(param)
        } else {
            Timber.i("Mqtt已经连接")
        }
    }



    /**
     * 连接Mqtt服务器
     */
    @Synchronized
    private fun connectMqttServer(param: MqttConnectParam) {
        try {
            if (isConninging) {
                Timber.i("TAG", "Mqtt正在连接")
                return
            }
            if (mClient == null) {
                mClient = MqttAndroidClient(param.context, param.serverUrl, param.id)
                mClient?.setCallback(this)
            }
            val conOpt = MqttConnectOptions()
            // 清除缓存
            conOpt.isCleanSession = param.isCleanSession
            // 设置超时时间，单位：秒
            conOpt.connectionTimeout = 5
            // 心跳包发送间隔，单位：秒
            conOpt.keepAliveInterval = param.keepAliveInterval
            // 用户名
//            conOpt.userName = param.user
            // 密码
//            conOpt.password = param.pass.toCharArray()
            isConninging = true
            mClient?.connect(conOpt, null, this)
        } catch (e: MqttException) {
            e.printStackTrace()
            callback!!.onConnectFailure(null, e)
            Timber.i("Mqtt连接失败")
        }
    }

    /**
     *
     * @return 返回Mqtt是否连接
     */
    fun isConnected(): Boolean {
        //如果实例==null
        return mClient?.isConnected?:false
    }

    /**
     * 失去连接
     * @param cause
     */
    override fun connectionLost(cause: Throwable?) {
        isConninging = false
        if (callback != null) {
            callback!!.onConnectLost()
        }
        Timber.i("连接丢失")
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (callback != null) {
            callback!!.onMessage(topic, message)
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        isConninging = false
        if (callback != null) {
            callback!!.onConnected()
            try {
                callback!!.subTopic().forEach {
                    Timber.i("${it.key} 订阅成功")
                    mClient?.subscribe(it.key, it.value)
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        Timber.i("Mqtt连接成功")
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        if (callback != null) {
            callback!!.onConnectFailure(asyncActionToken, exception)
        }
        isConninging = false
    }

    fun unsubscribe(topic: String) {
        mClient?.unsubscribe(topic)
    }
    /**
     * QoS0	无离线消息，在线消息只尝试推一次。	无离线消息，在线消息只尝试推一次。
     * QoS1	无离线消息，在线消息保证可达。	有离线消息，所有消息保证可达。
     * QoS2	无离线消息，在线消息保证只推一次。	暂不支持。
     */
    fun subscribe(topic: String, qos: Int) {
        mClient?.subscribe(topic, qos)
    }

    fun sendMsg(topic: String, qos: Int,retained:Boolean,msg:String){
        mClient
            ?.publish(topic,msg.toByteArray(),qos,retained)
    }
    fun sendMsg(msg:MessageBody){
        mClient?.publish(msg.topic,msg.msg.toByteArray(),msg.qos,msg.retained)
    }
    fun disconnect(){
        mClient?.disconnect()
//        mClient = null
    }

    interface MessageCallback {
        fun onMessage(topic: String?, message: MqttMessage?)
        fun onConnectFailure(asyncActionToken: IMqttToken?, exception: Throwable?)
        fun onConnected()
        fun onConnectLost()
        fun subTopic():MutableMap<String,Int>
    }
}