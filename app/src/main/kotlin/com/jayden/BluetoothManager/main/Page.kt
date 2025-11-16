package com.jayden.BluetoothManager.main

import com.jayden.BluetoothManager.R

sealed class Page(val pos: Int, val navId: Int) {
    data object BluetoothAdapter : Page(pos = 0, navId = R.id.menu_bluetooth_adapter)
}