package com.jayden.bluetooth.data.mapper

import android.bluetooth.BluetoothAdapter
import com.jayden.bluetooth.data.model.BluetoothState

object BluetoothAdapterMapper {
    fun Int.stateToSource(): BluetoothState {
        return when (this) {
            BluetoothAdapter.STATE_OFF -> BluetoothState.STATE_OFF
            BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.STATE_TURNING_ON
            BluetoothAdapter.STATE_ON -> BluetoothState.STATE_ON
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.STATE_TURNING_OFF
            else -> BluetoothState.STATE_OFF
        }
    }
}