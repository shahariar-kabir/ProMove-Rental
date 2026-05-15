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
                SupabaseManager.client.postgrest["messages"].insert(msgObject)
                // fetchMessages() // Refresh after sending
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMessagePoller() {
        lifecycleScope.launch {
            while (true) {
                fetchMessages()
                delay(3000) // Poll every 3 seconds for simple MVP
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
            
            adapter.setMessages(response)
            if (response.isNotEmpty()) {
                rvMessages.scrollToPosition(response.size - 1)
            }
        } catch (e: Exception) {
            // Silently fail for poller
        }
    }
}
