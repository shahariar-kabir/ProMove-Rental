package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.R
import com.example.promoverental.model.Message

class MessageAdapter(
    private val currentUserId: String,
    private val onDeleteClick: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        if (holder is SentViewHolder) holder.bind(message, onDeleteClick)
        else if (holder is ReceivedViewHolder) holder.bind(message, onDeleteClick)
    }

    override fun getItemCount(): Int = messages.size

    fun getMessages(): List<Message> = messages

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
        private val btnMenu = view.findViewById<ImageButton>(R.id.btnMenu)
        
        fun bind(message: Message, onDeleteClick: (Message) -> Unit) {
            tvMessage.text = message.message
            btnMenu.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Delete")
                popup.setOnMenuItemClickListener {
                    if (it.title == "Delete") {
                        onDeleteClick(message)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val btnMenu = view.findViewById<ImageButton>(R.id.btnMenu)

        fun bind(message: Message, onDeleteClick: (Message) -> Unit) {
            tvMessage.text = message.message
            btnMenu.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Delete")
                popup.setOnMenuItemClickListener {
                    if (it.title == "Delete") {
                        onDeleteClick(message)
                    }
                    true
                }
                popup.show()
            }
        }
    }
}
