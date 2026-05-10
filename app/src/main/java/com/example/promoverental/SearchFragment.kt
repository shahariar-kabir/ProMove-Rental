package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var houseAdapter: HouseAdapter
    private var allHouses: List<House> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val rvSearchResults = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        rvSearchResults.layoutManager = LinearLayoutManager(context)
        houseAdapter = HouseAdapter(emptyList()) { house ->
            val intent = Intent(context, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }
        rvSearchResults.adapter = houseAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchHouses()

        return view
    }

    private fun fetchHouses() {
        lifecycleScope.launch {
            try {
                allHouses = SupabaseManager.client.postgrest["houses"]
                    .select().decodeList<House>()
                houseAdapter.updateData(allHouses)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filter(text: String) {
        val filteredList = allHouses.filter { 
            it.title.contains(text, ignoreCase = true) || it.location.contains(text, ignoreCase = true)
        }
        houseAdapter.updateData(filteredList)
    }
}
