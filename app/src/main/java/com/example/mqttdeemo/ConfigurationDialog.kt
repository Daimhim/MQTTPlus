package com.example.mqttdeemo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.mqttdeemo.databinding.DialogConfigurationBinding
import timber.multiplatform.log.Timber

class ConfigurationDialog : Dialog {

    private lateinit var binding : DialogConfigurationBinding
    private  var serverBind: MqttServer.MyBind? = null
    var connectClickListener:View.OnClickListener ? = null
    private lateinit var subscribedChannelAdapter:SubscribedChannelAdapter
    private lateinit var msgSubTopicAdapter : ArrayAdapter<String>
    private lateinit var msgQOSAdapter : ArrayAdapter<Int>
    private val QOS = mutableListOf<Int>(
        0,1,2
    )
    constructor(context: Context):super(context,android.R.style.Theme_DeviceDefault_Light_NoActionBar){
        binding = DialogConfigurationBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.button.setOnClickListener {
            connectClickListener?.onClick(it)
        }
        subscribedChannelAdapter = SubscribedChannelAdapter()
        binding.linearLayout.adapter = subscribedChannelAdapter

        msgSubTopicAdapter = ArrayAdapter(context,android.R.layout.simple_spinner_item, mutableListOf())
        msgQOSAdapter = ArrayAdapter(context,android.R.layout.simple_spinner_item, QOS)

        binding.button2.setOnClickListener {
            subscribedChannelAdapter.addItem(TopicChannel())
        }
        binding.button3.setOnClickListener {
            if (serverBind?.isConnected() != true){
                Toast.makeText(context,"请先连接",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val efficientTopicChannels = subscribedChannelAdapter.getEfficientTopicChannels()
            Timber.i("efficientTopicChannels ${efficientTopicChannels.size}")
            val subTopic = mutableListOf<String>()
            val qoes = mutableListOf<Int>()
            efficientTopicChannels.forEach {
                serverBind?.unsubscribe(it.topic)
                serverBind?.subscribe(it.topic,it.qos)
                subTopic.add(it.topic)
                qoes.add(it.qos)
            }
            Toast.makeText(context,"已生效",Toast.LENGTH_SHORT).show()
            msgSubTopicAdapter.clear()
            msgSubTopicAdapter.addAll(subTopic)

            binding.spinner.setSelection(0,false)
            binding.spinner2.setSelection(0,false)
        }
        subscribedChannelAdapter.setOnItemClickListener { viewHolder, view, position ->
            val get = subscribedChannelAdapter.getData().get(position)
            serverBind?.unsubscribe(get.topic)
            subscribedChannelAdapter.removeItem(position)
            Toast.makeText(context,"已生效",Toast.LENGTH_SHORT).show()
        }
        binding.spinner.adapter = msgSubTopicAdapter
        binding.spinner2.adapter = msgQOSAdapter
        binding.button4.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //按空白处不能取消
        setCanceledOnTouchOutside(false);
        val decorView = window?.decorView ?:return
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN and View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.setSystemUiVisibility(option);
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.setStatusBarColor(Color.TRANSPARENT)
        //设置导航栏颜
        window?.setNavigationBarColor(Color.TRANSPARENT)
        //内容扩展到导航栏
        window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
    }

    fun show(bind: MqttServer.MyBind?) {
        serverBind = bind
        show()
    }

    fun updateUI(bind: MqttServer.MyBind?){
        serverBind = bind
        val config = bind?.getConfig()?: ServerConfig()
        binding.editTextTextPersonName.setText(config.serverUrl)
        binding.editTextTextPersonName2.setText(config.mqttId)
        binding.editTextTextPersonName3.setText(config.mqttUserName)
        binding.editTextTextPersonName4.setText(config.mqttPassword)
        updateServerConnectStateUI(bind)
        val mutableListOf = mutableListOf<TopicChannel>()
        val subTopic = mutableListOf<String>()
        bind?.getSubTopic()?.forEach {
            mutableListOf.add(TopicChannel(it.key,it.value))
            subTopic.add(it.key)
        }
        subscribedChannelAdapter.updateList(mutableListOf)
        msgSubTopicAdapter.clear()
        msgSubTopicAdapter.addAll(subTopic)
    }

    fun updateServerConnectStateUI(bind: MqttServer.MyBind?){
        if (bind?.isConnected() == true){
            binding.button.setText("断开")
        }else{
            binding.button.setText("连接")
        }
    }

    fun getServerConfig():ServerConfig{
        return ServerConfig(
            serverUrl = binding.editTextTextPersonName.text.toString(),
            mqttUserName = binding.editTextTextPersonName3.text.toString(),
            mqttPassword = binding.editTextTextPersonName4.text.toString(),
            mqttId = binding.editTextTextPersonName2.text.toString(),
            isCleanSession = binding.checkBox.isChecked,
            keepAliveInterval = binding.editTextTextPersonName7.text.toString().ifEmpty { "30" }.toInt()
        )
    }
    fun getMsgConfig():MsgConfig?{
        var topic = binding.editTextTextPersonName8.text.toString()
        if (topic.isEmpty()){
            topic = binding.spinner.selectedItem as (String?)?:""
        }
        if (topic.isEmpty()){
            Toast.makeText(context,"先配置通道",Toast.LENGTH_SHORT).show()
            return null
        }
        return MsgConfig(
            topic,
            binding.spinner2.selectedItem as Int,
            binding.checkBox2.isChecked
        )
    }
}