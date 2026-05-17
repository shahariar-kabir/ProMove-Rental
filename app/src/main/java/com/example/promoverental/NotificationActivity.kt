package com.example.promoverental

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.NotificationAdapter
import com.example.promoverental.model.Notification
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var rvNotifications: RecyclerView
    private lateinit var emptyState: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        rvNotifications = findViewById(R.id.rvNotifications)
        emptyState = findViewById(R.id.emptyState)

        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(emptyList())
        rvNotifications.adapter = adapter

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                val response = SupabaseManager.client.postgrest["notifications"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<Notification>()
                
                if (response.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvNotifications.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvNotifications.visibility = View.VISIBLE
                    adapter.updateData(response)
                }
            } catch (e: Exception) {
                // Handle missing table or other errors by showing empty state
                emptyState.visibility = View.VISIBLE
                rvNotifications.visibility = View.GONE
            }
        }
    }
}
