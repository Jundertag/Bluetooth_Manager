package com.jayden.BluetoothManager.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jayden.BluetoothManager.MainApplication
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var cutout: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            cutout = window.decorView.rootWindowInsets.displayCutout?.safeInsetTop ?: 0
        }
    }

    override fun onStart() {
        super.onStart()

        binding.pager.adapter = PageAdapter(this)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}