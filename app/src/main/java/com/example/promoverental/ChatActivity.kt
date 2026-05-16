package com.example.promoverental

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.MessageAdapter
import com.example.promoverental.model.Message
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private var receiverId: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        receiverId = intent.getStringExtra("receiver_id") ?: ""
        val ownerName = intent.getStringExtra("owner_name") ?: "Chat"
        currentUserId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

        if (receiverId.isEmpty()) {
            Toast.makeText(this, "Invalid receiver", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = ownerName
        toolbar.setNavigationOnClickListener { finish() }

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        val btnSend = findViewById<MaterialButton>(R.id.btnSend)

        adapter = MessageAdapter(currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                sendMessage(msg)
            }
        }

        startMessagePoller()
    }

    private fun sendMessage(text: String) {
        etMessage.setText("")
        lifecycleScope.launch {
            try {
                val msgObject = buildJsonObject {
                    put("sender_id", currentUserId)
                    put("receiver_id", receiverId)
                    put("message", text)
                }
                val response = SupabaseManager.client.postgrest["messages"].insert(msgObject)
                fetchMessages() 
            } catch (e: Exception) {
                // detailed error for debugging
                Toast.makeText(this@ChatActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun startMessagePoller() {
        lifecycleScope.launch {
            while (isActive) {
                fetchMessages()
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    private suspend fun fetchMessages() {
        try {
            val response = SupabaseManager.client.postgrest["messages"]
                .select {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", receiverId)
                            }
                            and {
                                eq("sender_id", receiverId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                    }
                    order("created_at", order = Order.ASCENDING)
                }.decodeList<Message>()
            
            if (response != adapter.getMessages()) {
                adapter.setMessages(response)
                if (response.isNotEmpty()) {
                    rvMessages.smoothScrollToPosition(response.size - 1)
                    markMessagesAsRead()
                }
            }
        } catch (e: Exception) {
            println("Chat Error: ${e.message}")
        }
    }

    private fun markMessagesAsRead() {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest["messages"].update(buildJsonObject {
                    put("is_read", true)
                }) {
                    filter {
                        eq("sender_id", receiverId)
                        eq("receiver_id", currentUserId)
                        eq("is_read", false)
                    }
                }
            } catch (e: Exception) { }
        }
    }
}
