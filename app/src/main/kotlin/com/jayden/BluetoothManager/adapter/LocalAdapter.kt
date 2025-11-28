package com.jayden.BluetoothManager.adapter

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.jayden.BluetoothManager.adapter.LocalAdapter.State.Companion.fromInt
import com.jayden.BluetoothManager.adapter.exception.AdapterNotOnException
import com.jayden.BluetoothManager.device.DeviceCompat
import com.jayden.BluetoothManager.context.ContextUtils
import com.jayden.BluetoothManager.permission.PermissionHelper

class LocalAdapter(
    private val manager: BluetoothManager
) {
    private val adapter = manager.adapter

    private val ctx: Context = ContextUtils.getAppContext()

    /**
     * Current state of the local adapter
     */
    val state: State
        get() = adapter.state.fromInt()

    /**
     * Represents the devices that are paired to this adapter (including disconnected devices)
     *
     * @throws SecurityException if device doesn't have [Manifest.permission.BLUETOOTH_CONNECT] permission
     */
    val pairedDevices: MutableSet<DeviceCompat>
        get() {
            if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                val result = mutableSetOf<DeviceCompat>()
                adapter.bondedDevices.forEach { device ->
                    val deviceCompat = DeviceCompat(device)
                    result.add(deviceCompat)
                }
                return result
            }
            else {
                throw SecurityException()
            }
        }

    /**
     * Whether the adapter is actively searching for devices that are advertising
     *
     * @throws SecurityException if app doesn't have [Manifest.permission.BLUETOOTH_SCAN] permission
     */
    val discovering: Boolean @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    get() {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return adapter.isDiscovering
        }
        else {
            throw SecurityException()
        }
    }

    /**
     * Get the adapter's friendly name. If any errors occur, simply returns an empty string
     *
     * @throws AdapterNotOnException if adapter is off
     */

    val name: String
        get() {
            return if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                adapter.name ?: ""
            } else {
                ""
            }
        }


    enum class State(val num: Int) {
        STATE_OFF(10),
        STATE_TURNING_ON(11),
        STATE_ON(12),
        STATE_TURNING_OFF(13);

        companion object {
            private val lookup = entries.associateBy { it.num }
            fun Int.fromInt(): State = lookup[this]!!
            fun State.toInt(): Int = this.num
        }
    }
}