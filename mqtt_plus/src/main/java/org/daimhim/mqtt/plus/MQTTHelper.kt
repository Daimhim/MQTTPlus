package org.daimhim.mqtt.plus

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.eclipse.paho.client.mqttv3.util.Debug
import java.util.concurrent.ScheduledExecutorService

/**
 * @bindMQTT()  用于生产MQTT并绑定
 * @connect()  用于与服务端产生连接
 * @disconnect()  用于与服务端断开连接
 * @subscribe()  用于接收服务端消息
 * @publish()  用于发消息到服务端
 * @close()  用于与服务端断开
 */
class MQTTHelper {
    /**
     * 真实的MqttClient
     */
    private var aClient: MqttAsyncClient? = null

    /**
     * 已订阅的通道记录
     */
    private val subscribes = mutableSetOf<String>()

    private lateinit var mqttClient: MqttClient

    private var isInit = false

    fun bindMQTT(
        serverURI: String?,
        clientId: String?,
        persistence: MqttClientPersistence?,
        executorService: ScheduledExecutorService?
    ) {
        checkInitialState()
        MQTTClient(
            serverURI,
            clientId,
            persistence,
            executorService,
        ).let { bindMQTT(it) }
    }

    fun bindMQTT(
        serverURI: String?,
        clientId: String?,
        persistence: MqttClientPersistence?
    ) {
        println("bindMQTT isInit $isInit")
        checkInitialState()
        MQTTClient(
            serverURI,
            clientId, persistence
        ).let { bindMQTT(it) }
    }

    fun bindMQTT(
        serverURI: String?,
        clientId: String?
    ) {
        checkInitialState()
        MQTTClient(
            serverURI,
            clientId
        ).let { bindMQTT(it) }
    }

    fun bindMQTT(mqtt: MqttClient) {
        checkInitialState()
        isInit = true
        mqttClient = mqtt
        if (mqtt is MQTTClient) {
            aClient = mqtt.getAsyncClient()
            return
        }
        helpTheWorld()
    }

    private fun helpTheWorld() {
        try {
            val mqttClientClass = MqttClient::class.java
            val aClientField = mqttClientClass.getDeclaredField("aClient")
            aClientField.isAccessible = true
            aClient = aClientField.get(mqttClient) as MqttAsyncClient?
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun checkInitialState(){
        if (isInit){
            throw IllegalStateException("mqttClient调用close之前不能重新初始化")
        }
    }
    fun connect() {
        mqttClient.connect()
    }

    fun connect(options: MqttConnectOptions?) {
        mqttClient.connect(options)
    }

    fun connect(
        options: MqttConnectOptions?,
        callback: IMqttActionListener? = null
    ) {
        aClient?.connect(options, null, callback)
    }

    fun connect(
        options: MqttConnectOptions?,
        userContext: Any? = null,
        callback: IMqttActionListener? = null
    ) {
        aClient?.connect(options, userContext, callback)
    }


    fun connectWithResult(options: MqttConnectOptions?): IMqttToken {
        return mqttClient.connectWithResult(options)
    }

    fun disconnect() {
        mqttClient.disconnect()
        subscribes.clear()
    }
    fun disconnect(quiesceTimeout:Long,callback:IMqttActionListener) {
        aClient?.disconnect(quiesceTimeout, null, callback)?.waitForCompletion()
        subscribes.clear()
    }

    fun disconnect(quiesceTimeout: Long) {
        mqttClient.disconnect(quiesceTimeout)
        subscribes.clear()
    }

    fun disconnectForcibly() {
        mqttClient.disconnectForcibly()
        subscribes.clear()
    }

    fun disconnectForcibly(disconnectTimeout: Long) {
        mqttClient.disconnectForcibly(disconnectTimeout)
        subscribes.clear()
    }

    fun disconnectForcibly(quiesceTimeout: Long, disconnectTimeout: Long) {
        mqttClient.disconnectForcibly(quiesceTimeout, disconnectTimeout)
        subscribes.clear()
    }

    fun disconnectForcibly(
        quiesceTimeout: Long,
        disconnectTimeout: Long,
        sendDisconnectPacket: Boolean
    ) {
        mqttClient.disconnectForcibly(quiesceTimeout, disconnectTimeout, sendDisconnectPacket)
        subscribes.clear()
    }

    fun publish(topic: String?, payload: ByteArray?, qos: Int, retained: Boolean) {
        mqttClient.publish(topic, payload, qos, retained)
    }

    fun publish(topic: String?, message: MqttMessage?) {
        mqttClient.publish(topic, message)
    }
    fun publish(topic: String?, message: MqttMessage?,callback:IMqttActionListener) {
        aClient?.publish(topic, message,null,callback)
    }

    fun setCallback(callback: MqttCallback?) {
        mqttClient.setCallback(callback)
    }

    fun getTopic(topic: String?): MqttTopic {
        return mqttClient.getTopic(topic)
    }

    fun isConnected(): Boolean {
        return isInit && mqttClient.isConnected
    }

    fun getClientId(): String {
        return mqttClient.getClientId()
    }

    fun getServerURI(): String {
        return mqttClient.getServerURI()
    }

    fun getPendingDeliveryTokens(): Array<IMqttDeliveryToken> {
        return mqttClient.getPendingDeliveryTokens()
    }

    fun setManualAcks(manualAcks: Boolean) {
        mqttClient.setManualAcks(manualAcks)
    }

    fun reconnect() {
        mqttClient.reconnect()
    }

    fun messageArrivedComplete(messageId: Int, qos: Int) {
        mqttClient.messageArrivedComplete(messageId, qos)
    }

    fun setTimeToWait(timeToWaitInMillis: Long) {
        mqttClient.setTimeToWait(timeToWaitInMillis)
    }

    fun getTimeToWait(): Long {
        return mqttClient.getTimeToWait()
    }

    fun getCurrentServerURI(): String {
        return mqttClient.getCurrentServerURI()
    }

    fun getDebug(): Debug {
        return mqttClient.getDebug()
    }

    fun subscribe(topicFilter: String) {
        mqttClient.subscribe(topicFilter)
        subscribes.add(topicFilter)
    }

    fun subscribe(topicFilters: Array<out String>) {
        mqttClient.subscribe(topicFilters)
        subscribes.addAll(topicFilters)
    }

    fun subscribe(topicFilter: String, qos: Int) {
        mqttClient.subscribe(topicFilter, qos)
        subscribes.add(topicFilter)
    }

    fun subscribe(topicFilters: Array<out String>, qos: IntArray?) {
        mqttClient.subscribe(topicFilters, qos)
        subscribes.addAll(topicFilters)
    }

    fun subscribe(topicFilter: String, messageListener: IMqttMessageListener?) {
        mqttClient.subscribe(topicFilter, messageListener)
        subscribes.add(topicFilter)
    }

    fun subscribe(
        topicFilters: Array<out String>,
        messageListeners: Array<out IMqttMessageListener>?
    ) {
        mqttClient.subscribe(topicFilters, messageListeners)
        subscribes.addAll(topicFilters)
    }

    fun subscribe(topicFilter: String, qos: Int, messageListener: IMqttMessageListener?) {
        mqttClient.subscribe(topicFilter, qos, messageListener)
        subscribes.add(topicFilter)
    }

    fun subscribe(
        topicFilters: Array<out String>,
        qos: IntArray?,
        messageListeners: Array<out IMqttMessageListener>?
    ) {
        mqttClient.subscribe(topicFilters, qos, messageListeners)
        subscribes.addAll(topicFilters)
    }

    fun subscribeWithResponse(topicFilter: String): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilter)
            .apply {
                subscribes.add(topicFilter)
            }
    }

    fun subscribeWithResponse(
        topicFilter: String,
        messageListener: IMqttMessageListener?
    ): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilter, messageListener)
            .apply {
                subscribes.add(topicFilter)
            }
    }

    fun subscribeWithResponse(topicFilter: String, qos: Int): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilter, qos)
            .apply {
                subscribes.add(topicFilter)
            }
    }

    fun subscribeWithResponse(
        topicFilter: String,
        qos: Int,
        messageListener: IMqttMessageListener?
    ): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilter, qos, messageListener)
            .apply {
                subscribes.add(topicFilter)
            }
    }

    fun subscribeWithResponse(topicFilters: Array<out String>): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilters)
            .apply {
                subscribes.addAll(topicFilters)
            }
    }

    fun subscribeWithResponse(
        topicFilters: Array<out String>,
        messageListeners: Array<out IMqttMessageListener>?
    ): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilters, messageListeners)
            .apply {
                subscribes.addAll(topicFilters)
            }
    }

    fun subscribeWithResponse(
        topicFilters: Array<out String>,
        qos: IntArray?
    ): IMqttToken {
        return mqttClient.subscribeWithResponse(topicFilters, qos)
            .apply {
                subscribes.addAll(topicFilters)
            }
    }

    fun subscribeWithResponse(
        topicFilters: Array<out String>,
        qos: IntArray?,
        messageListeners: Array<out IMqttMessageListener>?
    ): IMqttToken {
        return mqttClient?.subscribeWithResponse(topicFilters, qos, messageListeners)
            .apply {
                subscribes.addAll(topicFilters)
            }?:throw IllegalStateException("请务必先调用bingMQTT")
    }

    fun unsubscribe(topicFilter: String) {
        mqttClient.unsubscribe(topicFilter)
        subscribes.remove(topicFilter)
    }

    fun unsubscribe(topicFilters: Array<out String>) {
        mqttClient.unsubscribe(topicFilters)
        subscribes.removeAll(topicFilters.toSet())
    }

    fun isSubscribed(topicFilter: String): Boolean {
        return subscribes.contains(topicFilter)
    }

    fun getSubscribes():Set<String>{
        return subscribes
    }

    fun close() {
        mqttClient.close()
        release()
    }

    fun close(force: Boolean) {
        mqttClient.close(force)
        release()
    }
    private fun release(){
        val field = this::class.java.getField("mqttClient")
        field.isAccessible = true
        field.set(this,null)
        aClient = null
        isInit = false
    }
    private class MQTTClient : MqttClient {
        constructor(serverURI: String?, clientId: String?) : super(serverURI, clientId)
        constructor(
            serverURI: String?,
            clientId: String?,
            persistence: MqttClientPersistence?
        ) : super(serverURI, clientId, persistence)

        constructor(
            serverURI: String?,
            clientId: String?,
            persistence: MqttClientPersistence?,
            executorService: ScheduledExecutorService?
        ) : super(serverURI, clientId, persistence, executorService)

        fun getAsyncClient(): MqttAsyncClient {
            return aClient
        }
    }
}