package com.jayden.bluetooth.data.model

enum class BluetoothScanMode(val num: Int) {
    SCAN_MODE_NONE(20),
    SCAN_MODE_CONNECTABLE(21),
    SCAN_MODE_CONNECTABLE_DISCOVERABLE(23);

    companion object {
        private val lookup = entries.associateBy { it.num }

        fun Int.fromInt(): BluetoothScanMode {
            return lookup[this]!!
        }
    }
}