package com.jayden.bluetooth.data.adapter

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.device.A2dpDeviceCompat
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.forEach

class A2dpProfile(
    private val proxy: BluetoothA2dp
) : Profile() {

    override val rawProfile get() = proxy

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val connectedDevices: Flow<Set<DeviceEvent>> = callbackFlow {
        var devices: MutableSet<DeviceEvent> = mutableSetOf()

        devices = proxy.connectedDevices.map { DeviceEvent.Found(A2dpDeviceCompat(it)) }.toMutableSet()
        trySend(devices.toSet())
        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )

                        if (device == null) {
                            devices.add(
                                DeviceEvent.Error(
                                    msg = "received null device, ignoring...",
                                    `throw` = DeviceNotReceivedException()
                                )
                            )
                        } else {
                            devices.add(DeviceEvent.Found(A2dpDeviceCompat(device)))
                        }
                        trySend(devices.toSet())
                    }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )

                        if (device == null) {
                            devices.add(
                                DeviceEvent.Error(
                                    msg = "received null device, ignoring...",
                                    `throw` = DeviceNotReceivedException()
                                )
                            )
                        } else {
                            devices.forEach {
                                if (it is DeviceEvent.Found) {
                                    if (it.device.rawDevice == device) {
                                        devices.remove(it)
                                        trySend(devices.toSet())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}