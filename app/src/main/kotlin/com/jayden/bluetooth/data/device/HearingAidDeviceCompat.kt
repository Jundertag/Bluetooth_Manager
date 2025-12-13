package com.jayden.bluetooth.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHearingAid
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.adapter.HearingAidProfile
import com.jayden.bluetooth.data.device.DeviceCompat.ConnectionState.Companion.connectionStateFromInt
import com.jayden.bluetooth.data.device.DeviceEvent.HearingAidDeviceEvent
import com.jayden.bluetooth.data.device.exception.DeviceConnectionStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class HearingAidDeviceCompat(
    private val device: BluetoothDevice,
    private val proxy: BluetoothHearingAid
) : DeviceCompat(device) {

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val connectionStateFlow: Flow<HearingAidDeviceEvent> = callbackFlow {
        trySend(
            HearingAidDeviceEvent.ConnectionState(
                proxy.getConnectionState(device).connectionStateFromInt()
            )
        )

        val connectionStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val receivedDevice = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
                val receivedState =
                    intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)

                if (receivedDevice == null) {
                    trySend(
                        HearingAidDeviceEvent.Error(
                            msg = "received null device, ignoring...",
                            `throw` = DeviceNotReceivedException()
                        )
                    )
                } else if (receivedState == BluetoothDevice.ERROR) {
                    trySend(
                        HearingAidDeviceEvent.Error(
                            msg = "received null connection state, ignoring...",
                            `throw` = DeviceConnectionStateNotReceivedException()
                        )
                    )
                } else {
                    trySend(HearingAidDeviceEvent.ConnectionState(receivedState.connectionStateFromInt()))
                }
            }
        }
        ctx.registerReceiver(
            connectionStateReceiver,
            IntentFilter(BluetoothHearingAid.ACTION_CONNECTION_STATE_CHANGED)
        )
    }
}