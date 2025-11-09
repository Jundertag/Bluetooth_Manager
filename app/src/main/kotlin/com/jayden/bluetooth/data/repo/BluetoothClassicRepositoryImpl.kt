package com.jayden.bluetooth.data.repo

import com.jayden.bluetooth.data.model.BluetoothDeviceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class BluetoothClassicRepositoryImpl(
    private val dataSource: BluetoothDataSource
) : BluetoothClassicRepository {
    val classicDevices: Flow<MutableList<BluetoothDeviceModel>> = callbackFlow {

    }
}