package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.promoverental.adapter.ConversationAdapter
import com.example.promoverental.model.Conversation
import com.example.promoverental.model.Message
import com.example.promoverental.model.Profile
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class InboxActivity : AppCompatActivity() {

    private lateinit var adapter: ConversationAdapter
    private lateinit var rvInbox: RecyclerView
    private lateinit var emptyState: View
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        rvInbox = findViewById(R.id.rvInbox)
        emptyState = findViewById(R.id.emptyState)

        currentUserId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        
        rvInbox.layoutManager = LinearLayoutManager(this)
        adapter = ConversationAdapter(
            conversations = emptyList(),
            onItemClick = { otherUserId ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiver_id", otherUserId)
                intent.putExtra("owner_name", "Chat")
                startActivity(intent)
            },
            onItemLongClick = { conversation ->
                showDeleteConversationDialog(conversation)
            }
        )
        rvInbox.adapter = adapter

        fetchInbox(currentUserId)
    }

    private fun showDeleteConversationDialog(conversation: Conversation) {
        val otherUser = conversation.otherUser
        AlertDialog.Builder(this)
            .setTitle("Delete Conversation")
            .setMessage("Are you sure you want to delete all messages with ${otherUser.fullName ?: "this user"}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteConversation(otherUser.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversation(otherUserId: String) {
        lifecycleScope.launch {
            try {
                // Delete all messages where (sender=current AND receiver=other) OR (sender=other AND receiver=current)
                SupabaseManager.client.postgrest["messages"].delete {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", otherUserId)
                            }
                            and {
                                eq("sender_id", otherUserId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                    }
                }
                Toast.makeText(this@InboxActivity, "Conversation deleted", Toast.LENGTH_SHORT).show()
                fetchInbox(currentUserId)
            } catch (e: Exception) {
                Toast.makeText(this@InboxActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchInbox(currentUserId: String) {
        lifecycleScope.launch {
            try {
                // 1. Fetch all messages involving the user
                val allMessages = SupabaseManager.client.postgrest["messages"]
                    .select {
                        filter {
                            or {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                        order("created_at", order = Order.DESCENDING)
                    }.decodeList<Message>()

                // 2. Group by other user and find unique users
                val groupedMessages = allMessages.groupBy { 
                    if (it.senderId == currentUserId) it.receiverId else it.senderId 
                }

                val conversationList = mutableListOf<Conversation>()

                for ((otherUserId, messages) in groupedMessages) {
                    val lastMsg = messages.first()
                    val unreadCount = messages.count { it.receiverId == currentUserId && !it.isRead }
                    
                    // 3. Fetch profile for other user
                    val profile = try {
                        SupabaseManager.client.postgrest["profiles"]
                            .select { filter { eq("id", otherUserId) } }
                            .decodeSingle<Profile>()
                    } catch (e: Exception) {
                        Profile(id = otherUserId, fullName = "User ${otherUserId.take(5)}")
                    }

                    conversationList.add(Conversation(lastMsg, profile, unreadCount))
                }

                if (conversationList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvInbox.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvInbox.visibility = View.VISIBLE
                    adapter.updateData(conversationList)
                }
            } catch (e: Exception) {
                Toast.makeText(this@InboxActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
