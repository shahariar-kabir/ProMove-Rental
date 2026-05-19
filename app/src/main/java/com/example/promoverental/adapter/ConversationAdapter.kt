package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.promoverental.R
import com.example.promoverental.model.Conversation

class ConversationAdapter(
    private var conversations: List<Conversation>,
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUser: ImageView = view.findViewById(R.id.ivUser)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvUnreadBadge: TextView = view.findViewById(R.id.tvUnreadBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conv = conversations[position]
        val otherUser = conv.otherUser
        
        holder.tvName.text = otherUser.fullName ?: "User ${otherUser.id.take(5)}"
        holder.tvLastMessage.text = conv.lastMessage.message
        
        if (conv.unreadCount > 0) {
            holder.tvUnreadBadge.visibility = View.VISIBLE
            holder.tvUnreadBadge.text = conv.unreadCount.toString()
        } else {
            holder.tvUnreadBadge.visibility = View.GONE
        }

        if (!otherUser.avatarUrl.isNullOrEmpty()) {
            holder.ivUser.load(otherUser.avatarUrl) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            holder.ivUser.setImageResource(R.drawable.logo)
        }
        
        holder.itemView.setOnClickListener { onItemClick(otherUser.id) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(conv)
            true
        }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateData(newList: List<Conversation>) {
        conversations = newList
        notifyDataSetChanged()
    }
}
