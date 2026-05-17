package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AvailableHousesActivity : AppCompatActivity() {

    private lateinit var rvHouses: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var houseAdapter: HouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_houses)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Available Houses"
        toolbar.setNavigationOnClickListener { finish() }

        rvHouses = findViewById(R.id.rvMyHouses)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)
        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fabAddHouse)

        rvHouses.layoutManager = LinearLayoutManager(this)
        houseAdapter = HouseAdapter(
            houses = emptyList(),
            isOwner = true,
            onEditClick = { house ->
                val intent = Intent(this, AddHouseActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            },
            onDeleteClick = { house ->
                showDeleteConfirmation(house)
            },
            onItemClick = { house ->
                val intent = Intent(this, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
        )
        rvHouses.adapter = houseAdapter

        loadAvailableHouses()

        swipeRefresh.setOnRefreshListener { loadAvailableHouses() }
        fabAdd.setOnClickListener { startActivity(Intent(this, AddHouseActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadAvailableHouses()
    }

    private fun loadAvailableHouses() {
        swipeRefresh.isRefreshing = true
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

        lifecycleScope.launch {
            try {
                val allHouses = SupabaseManager.client.postgrest["houses"]
                    .select {
                        filter {
                            eq("owner_id", userId)
                        }
                    }.decodeList<House>()
                
                val availableHouses = allHouses.filter { it.status == "available" || it.status.isNullOrEmpty() }
                
                if (availableHouses.isEmpty()) {
                    rvHouses.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    rvHouses.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    houseAdapter.updateData(availableHouses)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AvailableHousesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showDeleteConfirmation(house: House) {
        AlertDialog.Builder(this)
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this listing?")
            .setPositiveButton("Delete") { _, _ ->
                deleteHouse(house)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHouse(house: House) {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest["houses"].delete {
                    filter {
                        eq("id", house.id ?: "")
                    }
                }
                Toast.makeText(this@AvailableHousesActivity, "Property deleted", Toast.LENGTH_SHORT).show()
                loadAvailableHouses()
            } catch (e: Exception) {
                Toast.makeText(this@AvailableHousesActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
