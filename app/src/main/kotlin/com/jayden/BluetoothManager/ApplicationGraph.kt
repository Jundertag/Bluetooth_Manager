package com.jayden.BluetoothManager

import android.bluetooth.BluetoothManager
import android.content.Context
import com.jayden.BluetoothManager.adapter.LocalAdapter
import com.jayden.BluetoothManager.permission.PermissionHelper
import com.jayden.BluetoothManager.context.ContextUtils

class ApplicationGraph(
    private val appContext: Context
) {
    init {
        ContextUtils.context = appContext
    }
    val manager: BluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    val localAdapter: LocalAdapter = LocalAdapter(manager)
}