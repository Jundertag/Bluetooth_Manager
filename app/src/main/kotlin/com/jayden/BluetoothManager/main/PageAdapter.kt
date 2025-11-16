package com.jayden.BluetoothManager.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jayden.BluetoothManager.adapter.LocalAdapterFragment

class PageAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {
    private val pages: List<Page> = listOf(Page.BluetoothAdapter)

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Page.BluetoothAdapter.pos -> LocalAdapterFragment()

            else -> LocalAdapterFragment()
        }
    }

    override fun getItemCount(): Int {
        return pages.size
    }

}