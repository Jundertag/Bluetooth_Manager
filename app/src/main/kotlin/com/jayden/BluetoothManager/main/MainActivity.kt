package com.jayden.BluetoothManager.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.jayden.BluetoothManager.MainApplication
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.adapter.LocalAdapterFragment
import com.jayden.BluetoothManager.databinding.ActivityMainBinding
import com.jayden.BluetoothManager.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var cutout: Int = 0

    private val fragments: Map<String, Fragment> = mapOf(LOCAL_ADAPTER_FRAGMENT to LocalAdapterFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            cutout = window.decorView.rootWindowInsets.displayCutout?.safeInsetTop ?: 0
        }

        supportFragmentManager.commitNow {
            for (fragment in fragments) {
                add(fragment.value, fragment.key)
                detach(fragment.value)
            }
            attach(fragments[LOCAL_ADAPTER_FRAGMENT]!!)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d(TAG, "clicked on menu id ${item.itemId}")
            if (item.itemId == R.id.menu_settings) {
                startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
                    setClass(applicationContext, SettingsActivity::class.java)
                })
            }
            true
        }

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            Log.v(TAG, "clicked on already selected item")
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCAL_ADAPTER_FRAGMENT = "local-adapter-fragment"
    }
}