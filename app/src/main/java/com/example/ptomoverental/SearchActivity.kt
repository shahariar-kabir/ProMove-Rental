package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptomoverental.adapter.HouseAdapter
import com.example.ptomoverental.model.House

class SearchActivity : AppCompatActivity() {
    private lateinit var adapter: HouseAdapter
    private val allHouses = listOf(
        House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment."),
        House("2", "Cozy Studio", "Banani, Dhaka", "$300/mo", 1, 1, "500 sqft", "Perfect for students or singles."),
        House("3", "Luxury Villa", "Uttara, Dhaka", "$1200/mo", 5, 4, "3500 sqft", "Spacious villa with a garden."),
        House("4", "Budget Flat", "Mirpur, Dhaka", "$200/mo", 2, 1, "800 sqft", "Affordable flat for small family.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        val rvResults = findViewById<RecyclerView>(R.id.rvSearchResults)
        val tvNoResults = findViewById<TextView>(R.id.tvNoResults)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        adapter = HouseAdapter(allHouses) { house ->
            val intent = Intent(this, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }

        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHouses(s.toString(), tvNoResults)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterHouses(query: String, tvNoResults: TextView) {
        val filteredList = allHouses.filter { 
            it.title.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true)
        }
        adapter.updateData(filteredList)
        tvNoResults.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}