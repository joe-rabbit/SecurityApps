package com.example.securityapps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securityapps.databinding.ActivityMainRecyclerViewBinding

class MainRecyclerView : AppCompatActivity() {
    private lateinit var binding : ActivityMainRecyclerViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainRecyclerView)
        binding.recyclerView.adapter = Recycler_Adapter()
    }
}