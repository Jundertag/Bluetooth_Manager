package com.jayden.bluetooth.core.dependency

import android.bluetooth.BluetoothManager
import android.content.Context
import com.jayden.bluetooth.data.repo.BluetoothRepository
import com.jayden.bluetooth.data.source.BluetoothDataSource

class DependencyResolver(
    private val appContext: Context
) {
    // dependencies
    private val bluetoothManager = appContext.getSystemService(BluetoothManager::class.java)

    // supply dependencies
    private val bluetoothDataSource: BluetoothDataSource = BluetoothDataSource(appContext, bluetoothManager)
    private val bluetoothRepo: BluetoothRepository = BluetoothRepository(bluetoothDataSource)
}