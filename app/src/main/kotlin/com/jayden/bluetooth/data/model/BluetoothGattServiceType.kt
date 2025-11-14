package com.jayden.bluetooth.data.model

enum class BluetoothGattServiceType(val num: Int) {
    SERVICE_TYPE_PRIMARY(0),
    SERVICE_TYPE_SECONDARY(1);

    companion object {
        private val lookup = entries.associateBy { it.num }

        fun Int.fromInt(): BluetoothGattServiceType {
            return lookup[this]!!
        }
    }
}