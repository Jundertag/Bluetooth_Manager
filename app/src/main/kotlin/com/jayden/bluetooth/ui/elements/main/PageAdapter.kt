package com.jayden.bluetooth.ui.elements.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jayden.bluetooth.ui.elements.main.pages.BluetoothHomeFragment

class PageAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {
    private val pages: List<Page> = listOf(Page.BluetoothHome)

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Page.BluetoothHome.pos -> BluetoothHomeFragment()

            else -> BluetoothHomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return pages.size
    }

}