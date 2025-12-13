package com.jayden.bluetooth.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.adapter.HeadsetProfile
import com.jayden.bluetooth.data.device.DeviceCompat.ConnectionState.Companion.connectionStateFromInt
import com.jayden.bluetooth.data.device.DeviceEvent.HeadsetDeviceEvent
import com.jayden.bluetooth.data.device.HeadsetDeviceCompat.AudioState.Companion.audioStateFromInt
import com.jayden.bluetooth.data.device.exception.DeviceAudioStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HeadsetDeviceCompat(
    private val device: BluetoothDevice,
    private val proxy: BluetoothHeadset
) : DeviceCompat(device) {

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val audioStateFlow: Flow<HeadsetDeviceEvent> = callbackFlow {
        val audioReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val receivedDevice = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
                val receivedState =
                    intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)

                if (receivedDevice == null) {
                    trySend(
                        HeadsetDeviceEvent.Error(
                            msg = "received null device, ignoring...",
                            `throw` = DeviceNotReceivedException()
                        )
                    )
                } else if (receivedState == BluetoothDevice.ERROR) {
                    trySend(
                        HeadsetDeviceEvent.Error(
                            msg = "received null audio state, ignoring...",
                            `throw` = DeviceAudioStateNotReceivedException()
                        )
                    )
                } else if (receivedDevice != device) {
                    trySend(HeadsetDeviceEvent.Error(msg = "received unrelated device, ignoring..."))
                } else {
                    trySend(HeadsetDeviceEvent.AudioState(receivedState.audioStateFromInt()))
                }
            }
        }
        ctx.registerReceiver(
            audioReceiver,
            IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        )

        awaitClose { ctx.unregisterReceiver(audioReceiver) }
    }

    enum class AudioState(val num: Int) {
        STATE_AUDIO_DISCONNECTED(10),
        STATE_AUDIO_CONNECTING(11),
        STATE_AUDIO_CONNECTED(12);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.audioStateFromInt(): AudioState = lookup[this] ?: STATE_AUDIO_DISCONNECTED
            fun AudioState.audioStateToInt(): Int = this.num
        }
    }
}