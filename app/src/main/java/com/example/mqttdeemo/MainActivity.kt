package com.example.mqttdeemo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.viewModels
import com.example.mqttdeemo.databinding.ActivityMainBinding
import timber.multiplatform.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private  lateinit var msgListAdapter : MsgListAdapter
    private  val mainViewModel  = viewModels<MainViewModel>()
    private var myBind: MqttServer.MyBind? = null
    private var configurationDialog: ConfigurationDialog? = null
    init {
        Timber.plant(Timber.DebugTree())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        msgListAdapter = MsgListAdapter()
        binding.rvMsgList.adapter = msgListAdapter
        val intent = Intent(this, MqttServer::class.java)
        startService(intent)
        connectServer()

        binding.tvSend.setOnClickListener {
            if (myBind?.isConnected() != true){
                Toast.makeText(this@MainActivity,"未连接",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val toString = binding.etMsgInput.text.toString()
            sendMsg(toString)
            binding.etMsgInput.setText("")
        }
        val connect = object : View.OnClickListener{
            override fun onClick(v: View?) {
                if (myBind == null){
                    connectServer()
                    return
                }
                if (myBind?.isConnected() == true){
                    binding.tvConnect.setText("断开")
                    myBind?.disconnect()
                }else{
                    binding.tvConnect.setText("连接")
                    myBind?.setConfig(configurationDialog?.getServerConfig()?:ServerConfig())
                    myBind?.connect()
                }
                updateConnectState()
            }
        }
        binding.tvConnect.setOnClickListener(connect)
        binding.etMsgInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                binding.rvMsgList.scrollToPosition(msgListAdapter.itemCount -1)
            }
        }
        binding.button3.setOnClickListener {
            if (configurationDialog == null){
                configurationDialog = ConfigurationDialog(this@MainActivity)
                configurationDialog?.connectClickListener = connect
            }
            configurationDialog?.updateUI(myBind)
            configurationDialog?.show()
        }
    }
    fun connectServer(){
        bindService(Intent(this, MqttServer::class.java),object : ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                myBind = service as MqttServer.MyBind
                myBind?.setCallbacl(object : IMqttPushCallback{
                    override fun asBinder(): IBinder? {
                        return null
                    }

                    override fun basicTypes(
                        anInt: Int,
                        aLong: Long,
                        aBoolean: Boolean,
                        aFloat: Float,
                        aDouble: Double,
                        aString: String?
                    ) {

                    }

                    override fun onPushMessage(msg: String?) {
                        runOnUiThread {
                            msgListAdapter.addMsg(MsgItem(msg?:"",System.currentTimeMillis()))
                            binding.rvMsgList.scrollToPosition(msgListAdapter.itemCount -1)
                        }
                    }

                    override fun updateConnectState() {
                        runOnUiThread { this@MainActivity.updateConnectState() }
                    }

                })
                runOnUiThread { updateConnectState() }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                myBind = null
                runOnUiThread { updateConnectState() }
            }

        },0)
    }
    private fun updateConnectState(){
        if (myBind?.isConnected() == true){
            binding.tvConnect.setText("断开")
        }else{
            binding.tvConnect.setText("连接")
        }
        configurationDialog?.updateServerConnectStateUI(myBind)
    }

    private fun sendMsg(text:String){
        val msgConfig = configurationDialog?.getMsgConfig()
        if (msgConfig == null){
            Toast.makeText(this,"请先配置",Toast.LENGTH_SHORT).show()
            return
        }
        if (text.isEmpty()){
            return
        }
        myBind?.sendMsg(msgConfig.topic,msgConfig.qos,msgConfig.retained,text)
        msgListAdapter.addMsg(MsgItem(text,System.currentTimeMillis(),true))

        binding.rvMsgList.scrollToPosition(msgListAdapter.itemCount -1)
    }


}