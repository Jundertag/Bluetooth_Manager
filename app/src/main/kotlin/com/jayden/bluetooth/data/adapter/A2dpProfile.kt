package com.jayden.bluetooth.data.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.device.A2dpDeviceCompat
import com.jayden.bluetooth.data.device.DeviceEvent.A2dpDeviceEvent
import com.jayden.bluetooth.data.device.exception.DeviceConnectionStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import com.jayden.bluetooth.utils.ContextUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.forEach

class A2dpProfile(
    private val proxy: BluetoothA2dp
) : Profile() {
    private val ctx: Context = ContextUtils.getAppContext()

    override val rawProfile get() = proxy

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val connectedDevicesFlow: Flow<List<A2dpDeviceEvent>> = callbackFlow {
        var devices = mutableListOf<A2dpDeviceEvent>()

        devices = proxy.connectedDevices.map {
            A2dpDeviceEvent.Found(A2dpDeviceCompat(it, proxy))
        }.toMutableList()
        trySend(devices)

        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val receivedDevice = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
                val receivedState =
                    intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)

                if (receivedDevice == null) {
                    devices.add(
                        A2dpDeviceEvent.Error(
                            msg = "received null device, ignoring...",
                            `throw` = DeviceNotReceivedException()
                        )
                    )
                } else if (receivedState == BluetoothDevice.ERROR) {
                    devices.add(
                        A2dpDeviceEvent.Error(
                            msg = "received null connection state, ignoring...",
                            `throw` = DeviceConnectionStateNotReceivedException()
                        )
                    )
                } else {
                    devices.add(
                        A2dpDeviceEvent.Found(A2dpDeviceCompat(receivedDevice, proxy))
                    )
                }
                trySend(devices)
            }
        }

        ctx.registerReceiver(
            deviceReceiver,
            IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        )
    }
}