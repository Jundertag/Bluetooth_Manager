package com.jayden.bluetooth.ui.main

import com.jayden.bluetooth.R

sealed class Page(val pos: Int, val navId: Int) {
    data object BluetoothHome : Page(pos = 0, navId = R.id.menu_bluetooth_home)
}