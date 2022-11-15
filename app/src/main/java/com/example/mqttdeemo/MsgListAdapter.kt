package com.example.mqttdeemo

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttdeemo.databinding.ItemMsgListBinding
import java.text.SimpleDateFormat
import java.util.*

class MsgListAdapter :
    RecyclerView.Adapter<MsgListAdapter.MsgViewHolder>() {
    private val msgList = mutableListOf<MsgItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgViewHolder {
        return MsgViewHolder(ItemMsgListBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MsgViewHolder, position: Int) {
        val item = msgList.get(position)
        holder.binding.tvMsgContent.text = item.msg
        holder.binding.tvMsgContent.gravity = if (item.isMe) Gravity.RIGHT else Gravity.LEFT
        holder.binding.tvUser.text = "${(if (item.isMe) "我" else "他")} ${item.time.toDateStr()}"
        holder.binding.clRoot.layoutDirection = if (item.isMe) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }
    fun addMsg(item: MsgItem){
        msgList.add(item)
        notifyItemInserted(msgList.size-1)
    }

    class MsgViewHolder(val binding: ItemMsgListBinding) : RecyclerView.ViewHolder(binding.root) {

    }
    fun Long.toDateStr(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val date = Date(this)
        val format = SimpleDateFormat(pattern)
        return format.format(date)
    }

    companion object{
        val MSG_DIFF_UTIL = object : DiffUtil.ItemCallback<MsgItem>() {
            override fun areItemsTheSame(oldItem: MsgItem, newItem: MsgItem): Boolean {
                return oldItem.equals(newItem)
            }

            override fun areContentsTheSame(oldItem: MsgItem, newItem: MsgItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}