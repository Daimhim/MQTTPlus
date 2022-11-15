package com.example.mqttdeemo

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mqttdeemo.databinding.AdapterSubscribedChannelBinding
import org.daimhim.widget.sa.SimpleRVAdapter
import org.daimhim.widget.sa.SimpleViewHolder
import java.lang.ref.SoftReference

class SubscribedChannelAdapter : SimpleRVAdapter(), IAdapterTextWatcher {
    private val topicChannels = mutableListOf<TopicChannel>()


    override fun getItemCount(): Int {
        return topicChannels.size
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SCViewHolder(AdapterSubscribedChannelBinding
            .inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindDataViewHolder(holder: SimpleViewHolder, position: Int) {
        val get = topicChannels.get(position)
        val bind = AdapterSubscribedChannelBinding.bind(holder.itemView)
        bind.editTextTextPersonName5.setText(get.topic)
        bind.editTextTextPersonName6.setText(get.qos.toString())
        (holder as SCViewHolder).let {
            it.bindAdapterTextWatcher(bind.editTextTextPersonName5,this@SubscribedChannelAdapter)
            it.bindAdapterTextWatcher(bind.editTextTextPersonName6,this@SubscribedChannelAdapter)
        }
        holder.bindClickListener(bind.imageView)
    }

    fun updateList(list:MutableList<TopicChannel>){
        topicChannels.clear()
        topicChannels.addAll(list)
        notifyDataSetChanged()
    }

    fun addItem(channel: TopicChannel){
        topicChannels.add(channel)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        topicChannels.removeAt(position)
        notifyItemRemoved(position)
    }
    fun getData():List<TopicChannel>{
        return topicChannels
    }

    fun getEfficientTopicChannels():List<TopicChannel>{
        return topicChannels
            .filter {
                it.topic.isNotEmpty()
            }
    }


    class SCViewHolder(binding :AdapterSubscribedChannelBinding) : SimpleViewHolder(binding.root) {
        private var bindTextWatcher : MutableMap<Int,BindAdapterTextWatcher>? = null

        fun bindAdapterTextWatcher(tv:TextView,tw:IAdapterTextWatcher){
            if(bindTextWatcher == null){
                bindTextWatcher = mutableMapOf()
            }
            val batw = if (bindTextWatcher?.containsKey(tv.id) == true){
                bindTextWatcher?.get(tv.id)!!
            }else{
                BindAdapterTextWatcher()
                    .also {
                        tv.addTextChangedListener(it)
                    }
            }
            batw.position = absoluteAdapterPosition
            batw.bindHolder(tw,tv)
            bindTextWatcher?.put(tv.id,batw)
        }

    }

    class BindAdapterTextWatcher : TextWatcher {
        var position: Int = -1
        private var adapterTextWatcher:SoftReference<IAdapterTextWatcher>? = null
        private var et:SoftReference<TextView>? = null
        fun bindHolder(textWatcher:IAdapterTextWatcher,tv:TextView){
            adapterTextWatcher = SoftReference(textWatcher)
            et = SoftReference(tv)
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            adapterTextWatcher?.get()?.afterTextChanged(et?.get(),position,s)
        }

    }

    override fun afterTextChanged(view: TextView?, position: Int, s: Editable?) {
        val topicChannel = topicChannels.get(position)
        when(view?.id){
            R.id.editTextTextPersonName5->{
                topicChannels.set(position,topicChannel.copy(topic = s?.toString()?:""))
            }
            R.id.editTextTextPersonName6->{
                topicChannels.set(position,topicChannel.copy(qos = if (s?.isEmpty()==true) 0 else s?.toString()?.toInt()?:0))
            }
            else->{}
        }
    }

}
public interface IAdapterTextWatcher{
    fun afterTextChanged(view: TextView?, position: Int, s: Editable?)
}