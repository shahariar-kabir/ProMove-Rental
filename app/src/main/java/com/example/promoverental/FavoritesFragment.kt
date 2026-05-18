package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.Favorite
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var houseAdapter: HouseAdapter
    private lateinit var emptyState: View
    private lateinit var rvFavorites: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        rvFavorites = view.findViewById(R.id.rvFavorites)
        emptyState = view.findViewById(R.id.emptyState)
        rvFavorites.layoutManager = LinearLayoutManager(context)

        houseAdapter = HouseAdapter(emptyList()) { house ->
            val intent = Intent(context, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }
        rvFavorites.adapter = houseAdapter

        fetchFavorites()

        return view
    }

    private fun fetchFavorites() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                val response = SupabaseManager.client.postgrest["favorites"]
                    .select(Columns.raw("*, house:houses(*)")) {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<Favorite>()
                
                val favoriteHouses = response.mapNotNull { it.house }
                
                if (favoriteHouses.isEmpty()) {
                    rvFavorites.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    rvFavorites.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    houseAdapter.updateData(favoriteHouses)
                }
            } catch (e: Exception) {
                rvFavorites.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            }
        }
    }
}
