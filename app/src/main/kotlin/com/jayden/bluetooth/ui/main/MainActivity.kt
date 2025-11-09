package com.jayden.bluetooth.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.jayden.bluetooth.R
import com.jayden.bluetooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            val cutout = window.decorView.rootWindowInsets.displayCutout?.safeInsetTop ?: 0
            binding.tabLayout.setPaddingRelative(0, cutout, 0, 0)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.pager.adapter = PageAdapter(this)

        val mediator = TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                Page.BluetoothHome.pos -> resources.getString(R.string.menu_bluetooth_home)

                else -> ""
            }
        }

        mediator.attach()

        binding.imageView.setImageDrawable(applicationContext.getDrawable(R.drawable.ic_launcher_foreground))
    }
}