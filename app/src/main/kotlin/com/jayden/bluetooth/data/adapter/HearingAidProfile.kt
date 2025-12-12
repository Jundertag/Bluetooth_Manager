package com.jayden.bluetooth.data.adapter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHearingAid
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jayden.bluetooth.data.device.DeviceCompat
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.data.device.exception.DeviceConnectionStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

open class HearingAidProfile(private val proxy: BluetoothHearingAid) : Profile() {
    override val rawProfile get() = proxy

    val devicesFlow: Flow<List<DeviceEvent>> = callbackFlow {
        var devices: MutableList<DeviceEvent> = mutableListOf()
        devices = proxy.getDevicesMatchingConnectionStates(intArrayOf(
            BluetoothProfile.STATE_CONNECTED,
            BluetoothProfile.STATE_CONNECTING,
            BluetoothProfile.STATE_DISCONNECTED,
            BluetoothProfile.STATE_DISCONNECTING
        )).map {
            DeviceEvent.Found(DeviceCompat(it))
        }.toMutableList()
        trySend(devices)
        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                devices.removeIf { event ->
                    event is DeviceEvent.Error
                }
                val received = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)

                if (received == null) {
                    devices.add(
                        DeviceEvent.Error(
                            msg = "received null device, ignoring...",
                            `throw` = DeviceNotReceivedException()
                        )
                    )

                } else if (state == BluetoothDevice.ERROR) {
                    devices.add(DeviceEvent.Error(msg = "received null state, ignoring...", `throw` = DeviceConnectionStateNotReceivedException()))
                } else {
                    devices.add(DeviceEvent.Found(DeviceCompat(received)))
                }
                trySend(devices)
            }
        }
    }
}