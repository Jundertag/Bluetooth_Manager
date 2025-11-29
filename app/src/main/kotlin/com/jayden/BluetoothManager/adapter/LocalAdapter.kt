package com.jayden.BluetoothManager.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.jayden.BluetoothManager.adapter.LocalAdapter.State.Companion.fromInt
import com.jayden.BluetoothManager.adapter.exception.AdapterNotOnException
import com.jayden.BluetoothManager.device.DeviceCompat
import com.jayden.BluetoothManager.context.ContextUtils
import com.jayden.BluetoothManager.permission.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalAdapter(
    manager: BluetoothManager
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
    val pairedDevices: MutableSet<DeviceCompat> @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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
    val name: String @Suppress("MissingPermission")
        get() {
            return if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                adapter.name ?: ""
            } else {
                ""
            }
        }

    private var discoveryReceiverRegistered: Boolean = false

    private val _discoveredDevices: MutableStateFlow<MutableList<DeviceCompat>> = MutableStateFlow(mutableListOf())
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION])
    val discoveredDevices = _discoveredDevices.asStateFlow()

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

                val rssi: Int = intent.getIntExtra(BluetoothDevice.EXTRA_RSSI, BluetoothDevice.ERROR)

                if (device != null) {
                    val compat = DeviceCompat(device).also {
                        it.rssi = rssi
                    }

                    _discoveredDevices.update {
                        it.add(compat)
                        it
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery() {
        adapter.cancelDiscovery()
        adapter.startDiscovery()

        val intentFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
        }
        if (!discoveryReceiverRegistered)
            ctx.registerReceiver(discoveryReceiver, intentFilter)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        adapter.cancelDiscovery()
        if (discoveryReceiverRegistered)
            ctx.unregisterReceiver(discoveryReceiver)
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