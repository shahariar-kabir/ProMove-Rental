package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.R
import com.example.promoverental.model.Message
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth

class MessageAdapter(private val currentUserId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) holder.bind(message)
        else if (holder is ReceivedViewHolder) holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    fun setMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        fun bind(message: Message) {
            tvMessage.text = message.message
        }
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        fun bind(message: Message) {
            tvMessage.text = message.message
        }
    }
}
