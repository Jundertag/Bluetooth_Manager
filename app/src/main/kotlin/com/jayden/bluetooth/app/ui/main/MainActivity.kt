package com.jayden.bluetooth.app.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.jayden.bluetooth.R
import com.jayden.bluetooth.app.ui.main.pages.LocalAdapterFragment
import com.jayden.bluetooth.databinding.ActivityMainBinding
import com.jayden.bluetooth.app.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var cutout: Int = 0

    private val fragments: List<String> = listOf(LOCAL_ADAPTER_FRAGMENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate(savedInstanceState = $savedInstanceState)")
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            cutout = window.decorView.rootWindowInsets.displayCutout?.safeInsetTop ?: 0
        }

        val nightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                setNightMode(mode = true, recreate = false)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                setNightMode(mode = false, recreate = false)
            }

            else -> {
                setNightMode(mode = false, recreate = false)
            }
        }


        if (savedInstanceState == null) {
            Log.i(TAG, "savedInstanceState == null")
            Log.i(TAG, "adding and detaching all fragments via the fragment manager")
            supportFragmentManager.commitNow {
                for (fragment in fragments) {
                    val newFrag = newFragment(fragment)
                    add(R.id.fragment_container, newFrag, fragment)
                    detach(newFrag)
                }
                runOnCommit {
                    Log.d(TAG, "attaching LOCAL_ADAPTER_FRAGMENT")
                    attach(requireFragment(fragments.first()))
                }
            }
        } else {
            Log.i(TAG, "savedInstanceState != null")
            Log.i(TAG, "detaching all fragments via the fragment manager")
            supportFragmentManager.commitNow {
                for (fragment in fragments) {
                    detach(requireFragment(fragment))
                }
                runOnCommit {
                    Log.d(TAG, "attaching LOCAL_ADAPTER_FRAGMENT")
                    attach(requireFragment(fragments.first()))
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.v(TAG, "onConfigurationChanged(newConfig = $newConfig)")
        val nightMode = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK)

        when (nightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                Log.d(TAG, $$"onConfigurationChanged$nightMode = true")
                setNightMode(mode = true, recreate = true)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                Log.d(TAG, $$"onConfigurationChanged$nightMode = false")
                setNightMode(mode = false, recreate = true)
            }
            else -> {
                Log.w(TAG, $$"onConfigurationChanged$nightMode = UNDEFINED")
                setNightMode(mode = true, recreate = true)
            }
        }

        super.onConfigurationChanged(newConfig)
    }

    override fun onStart() {
        Log.v(TAG, "onStart()")
        super.onStart()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d(TAG, "clicked on ${item.title} menu")
            if (item.itemId == R.id.menu_settings) {
                startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
                    setClass(applicationContext, SettingsActivity::class.java)
                })
            }
            false
        }

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            Log.v(TAG, "clicked on already selected item ${item.title}")
        }
    }

    override fun onResume() {
        Log.v(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.v(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.v(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        Log.w(TAG, "onDestroy()")
        _binding = null
        super.onDestroy()
    }

    private fun getFragment(tag: String): Fragment? {
        return supportFragmentManager.findFragmentByTag(tag)
    }

    private fun requireFragment(tag: String): Fragment {
        return getFragment(tag)!!
    }

    private fun newFragment(tag: String): Fragment {
        return when (tag) {
            LOCAL_ADAPTER_FRAGMENT -> LocalAdapterFragment()
            else -> throw IllegalStateException("expand list?")
        }
    }

    private fun setNightMode(mode: Boolean, recreate: Boolean) {
        Log.i(TAG, "setNightMode(mode = $mode, recreate = $recreate)")
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !mode
            isAppearanceLightNavigationBars = !mode
        }
        if (recreate) recreate()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCAL_ADAPTER_FRAGMENT = "local-adapter-fragment"
    }
}