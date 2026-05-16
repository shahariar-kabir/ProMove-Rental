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

class MyHousesActivity : AppCompatActivity() {

    private lateinit var rvMyHouses: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var houseAdapter: HouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_houses)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvMyHouses = findViewById(R.id.rvMyHouses)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)
        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fabAddHouse)

        rvMyHouses.layoutManager = LinearLayoutManager(this)
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
        rvMyHouses.adapter = houseAdapter

        loadMyHouses()

        swipeRefresh.setOnRefreshListener { loadMyHouses() }
        fabAdd.setOnClickListener { startActivity(Intent(this, AddHouseActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadMyHouses()
    }

    private fun loadMyHouses() {
        swipeRefresh.isRefreshing = true
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

        lifecycleScope.launch {
            try {
                val houses = SupabaseManager.client.postgrest["houses"]
                    .select {
                        filter {
                            eq("owner_id", userId)
                        }
                    }.decodeList<House>()
                
                if (houses.isEmpty()) {
                    rvMyHouses.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    rvMyHouses.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    houseAdapter.updateData(houses)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyHousesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@MyHousesActivity, "Property deleted", Toast.LENGTH_SHORT).show()
                loadMyHouses()
            } catch (e: Exception) {
                Toast.makeText(this@MyHousesActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
