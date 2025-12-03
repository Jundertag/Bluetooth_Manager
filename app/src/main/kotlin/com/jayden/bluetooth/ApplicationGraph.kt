package com.jayden.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import com.jayden.bluetooth.adapter.LocalAdapter
import com.jayden.bluetooth.context.ContextUtils

class ApplicationGraph(
    private val appContext: Context
) {
    init {
        ContextUtils.context = appContext
    }
    val manager: BluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    val localAdapter: LocalAdapter = LocalAdapter(manager)
}