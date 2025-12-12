package com.jayden.bluetooth.data.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.device.DeviceEvent.HeadsetDeviceEvent
import com.jayden.bluetooth.data.device.HeadsetDeviceCompat
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import com.jayden.bluetooth.utils.ContextUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HeadsetProfile(
    private val proxy: BluetoothHeadset
) : Profile() {
    override val rawProfile get() = proxy

    private val ctx: Context = ContextUtils.getAppContext()
    /**
     * devices that are connected to the [BluetoothHeadset] profile
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val connectedDevices: Flow<Set<HeadsetDeviceEvent>> = callbackFlow {
        val connectedDevices = mutableSetOf<HeadsetDeviceEvent>()
        proxy.connectedDevices.forEach { device ->
            connectedDevices.add(HeadsetDeviceEvent.Found(HeadsetDeviceCompat(device)))
        }
        trySend(connectedDevices)

        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                connectedDevices.removeIf { device ->
                    device is HeadsetDeviceEvent.Error
                }
                when (intent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )

                        if (device == null) {
                            connectedDevices.add(
                                HeadsetDeviceEvent.Error(
                                    msg = "received null device, ignoring...",
                                    `throw` = DeviceNotReceivedException()
                                )
                            )
                        } else {
                            connectedDevices.add(HeadsetDeviceEvent.Found(HeadsetDeviceCompat(device)))
                        }
                    }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )

                        if (device == null) {
                            connectedDevices.add(
                                HeadsetDeviceEvent.Error(
                                    msg = "received null device, ignoring...",
                                    `throw` = DeviceNotReceivedException()
                                )
                            )
                        }
                    }
                }
            }
        }
        ctx.registerReceiver(deviceReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        })

        awaitClose { ctx.unregisterReceiver(deviceReceiver) }
    }

    /**
     * devices that are discovered and compatible with the [BluetoothHeadset] profile
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    val discoveredDevices: Flow<Set<HeadsetDeviceEvent>> = callbackFlow {
        val discoveredDevices = mutableSetOf<HeadsetDeviceEvent>()

        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {

                    }
                }
            }
        }
    }
}