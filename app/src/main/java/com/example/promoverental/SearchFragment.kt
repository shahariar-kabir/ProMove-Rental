package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House

class SearchFragment : Fragment() {

    private lateinit var houseAdapter: HouseAdapter
    private val allHouses = listOf(
        House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment."),
        House("2", "Cozy Studio", "Banani, Dhaka", "$300/mo", 1, 1, "500 sqft", "Perfect for students."),
        House("3", "Luxury Villa", "Uttara, Dhaka", "$1500/mo", 5, 4, "4000 sqft", "Huge villa with a pool."),
        House("4", "Office Space", "Dhanmondi, Dhaka", "$800/mo", 0, 1, "1500 sqft", "Ideal for startups.")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val rvSearchResults = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        rvSearchResults.layoutManager = LinearLayoutManager(context)
        houseAdapter = HouseAdapter(allHouses) { house ->
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

        return view
    }

    private fun filter(text: String) {
        val filteredList = allHouses.filter { 
            it.title.contains(text, ignoreCase = true) || it.location.contains(text, ignoreCase = true)
        }
        houseAdapter.updateData(filteredList)
    }
}
