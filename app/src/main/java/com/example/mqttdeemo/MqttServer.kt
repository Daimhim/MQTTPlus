package com.example.mqttdeemo

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast

import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.multiplatform.log.Timber


class MqttServer : Service(),Mqtt.MessageCallback {
    companion object{
        var serverConfig = ServerConfig()
        val subTopic = mutableMapOf<String,Int>()
    }



    private var mMqtt: Mqtt? = null
    private var mMegCallback: IMqttPushCallback? = null
    private val mBinder: IBinder = MyBind()

    override fun onCreate() {
        super.onCreate()
        if (mMqtt==null){mMqtt=Mqtt.getInstance();}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mMqtt==null){mMqtt=Mqtt.getInstance();}
//        mMqtt.setmTopic(getTopic());
//        mMqtt?.tryConnect(getMqttConnectParam(),this);
        return super.onStartCommand(intent, flags, startId)
    }
    private fun getMqttConnectParam(): MqttConnectParam {
        return MqttConnectParam(this,
            serverConfig.serverUrl,
            serverConfig.mqttId,
            serverConfig.mqttUserName,
            serverConfig.mqttPassword,
            serverConfig.isCleanSession,
            serverConfig.keepAliveInterval,
        )
    }
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onMessage(topic: String?, message: MqttMessage?) {
        Timber.i("onMessage" + String(message?.payload?:return))
        mMegCallback?.updateConnectState()
        mMegCallback?.onPushMessage(String(message?.payload?:return))
    }

    override fun onConnectFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        mMegCallback?.updateConnectState()
        Toast.makeText(this, "连接失败" + exception?.message, Toast.LENGTH_SHORT).show()
        Timber.i(exception,"连接失败" + exception?.message)
    }

    override fun onConnected() {
        mMegCallback?.updateConnectState()
        Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectLost() {
        mMegCallback?.updateConnectState()
    }

    override fun subTopic(): MutableMap<String, Int> {
        return subTopic
    }

    inner class MyBind : Binder() {
        fun setCallbacl(callbacl: IMqttPushCallback) {
            mMegCallback = callbacl
        }
        fun sendMsg(topic: String, qos: Int,retained:Boolean,text:String){
            mMqtt?.sendMsg(topic,qos,retained,text)
        }
        fun  isConnected():Boolean{
            return mMqtt?.isConnected()?:false
        }
        fun connect(){
            mMqtt?.tryConnect(getMqttConnectParam(),this@MqttServer)
        }
        fun disconnect(){
            mMqtt?.disconnect()
        }

        fun setConfig(
            config:ServerConfig,
        ) {
            serverConfig = config
        }
        fun getConfig():ServerConfig{
            return serverConfig
        }

        fun unsubscribe(topic: String) {
            mMqtt?.unsubscribe(topic)
            subTopic.remove(topic)
        }

        fun subscribe(topic: String, qos: Int) {
            mMqtt?.subscribe(topic, qos)
            subTopic.put(topic,qos)
        }

        fun getSubTopic():MutableMap<String,Int>{
            return subTopic
        }
    }
}