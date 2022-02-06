package com.utc.donlyconan.media.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}