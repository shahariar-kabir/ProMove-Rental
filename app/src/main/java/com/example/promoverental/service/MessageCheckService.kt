package com.example.promoverental.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.promoverental.model.Message
import com.example.promoverental.utils.NotificationHelper
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.*

class MessageCheckService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastCheckedTimestamp: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPolling()
        return START_STICKY
    }

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                val user = SupabaseManager.client.auth.currentUserOrNull()
                if (user != null) {
                    try {
                        val unreadMessages = SupabaseManager.client.postgrest["messages"]
                            .select {
                                filter {
                                    eq("receiver_id", user.id)
                                    eq("is_read", false)
                                }
                            }.decodeList<Message>()

                        if (unreadMessages.isNotEmpty()) {
                            val lastMsg = unreadMessages.last()
                            // Only notify if it's a new unread message since last check
                            if (lastMsg.createdAt != lastCheckedTimestamp) {
                                NotificationHelper.showNotification(
                                    applicationContext,
                                    "New Message Received",
                                    lastMsg.message
                                )
                                lastCheckedTimestamp = lastMsg.createdAt
                            }
                        }
                    } catch (e: Exception) {
                        println("Polling Error: ${e.message}")
                    }
                }
                delay(10000) // Check every 10 seconds for MVP
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
