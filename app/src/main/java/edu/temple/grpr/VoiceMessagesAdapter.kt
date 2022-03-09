package edu.temple.grpr

import android.annotation.SuppressLint
import android.service.autofill.TextValueSanitizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class VoiceMessagesAdapter(_messages: VoiceMessagesList, _user: String, _onClick: (VoiceMessage) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val messages = _messages
    val onClick = _onClick
    val user = _user

    internal val VIEW_TYPE_INCOMING = 1
    internal val VIEW_TYPE_OUTGOING = 2

    class InMessagesViewHolder (layout : View, onClick: (VoiceMessage) -> Unit): RecyclerView.ViewHolder (layout){
        val username: TextView
        val incomingTime: TextView
        val incomingPlay: ImageView
        lateinit var vm: VoiceMessage
        init {
            username = layout.findViewById(R.id.userNameTextView)
            incomingTime = layout.findViewById(R.id.incomingTimeTextView)
            incomingPlay = layout.findViewById(R.id.incomingImageView)
            incomingPlay.setOnClickListener {
                onClick(vm)
            }
        }
    }

    class OutMessagesViewHolder (layout : View, onClick: (VoiceMessage) -> Unit): RecyclerView.ViewHolder (layout){
        val outgoingTime: TextView
        val outgoingPlay: ImageView
        lateinit var vm: VoiceMessage
        init {
            outgoingTime = layout.findViewById(R.id.incomingTimeTextView)
            outgoingPlay = layout.findViewById(R.id.incomingImageView)
            outgoingPlay.setOnClickListener {
                onClick(vm)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INCOMING) {
            InMessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.incoming_message, parent, false), onClick)
        } else OutMessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.outgoing_message,parent,false), onClick)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val time = dateFormatter(messages.get(position).time)
        if (messages.get(position).username == user) {
            (holder as OutMessagesViewHolder).outgoingTime.text =time.toString()
            (holder as OutMessagesViewHolder).vm = messages.get(position)
        } else {
            (holder as InMessagesViewHolder).username.text=messages.get(position).username
            (holder as InMessagesViewHolder).incomingTime.text=time.toString()
            (holder as InMessagesViewHolder).vm=messages.get(position)
        }
    }

    override fun getItemCount(): Int {
        return messages.size()
    }


    @SuppressLint("SimpleDateFormat")
    fun dateFormatter(epoch: Long): String {
       val timeD = Date(epoch)
       val sdf = SimpleDateFormat("MM-dd HH:mm")
        return  sdf.format(timeD)
    }


}

