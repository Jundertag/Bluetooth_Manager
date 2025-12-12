package com.jayden.bluetooth.data.device

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.device.A2dpDeviceCompat.PlayState.Companion.playStateFromInt
import com.jayden.bluetooth.data.device.DeviceCompat.ConnectionState.Companion.connectionStateFromInt
import com.jayden.bluetooth.data.device.DeviceEvent.A2dpDeviceEvent
import com.jayden.bluetooth.data.device.exception.A2dpDeviceConnectionStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.A2dpDevicePlayStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import com.jayden.bluetooth.utils.ContextUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * subclass of [DeviceCompat] used for A2dp specific operations
 */
class A2dpDeviceCompat(private val device: BluetoothDevice) : DeviceCompat(device) {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val playState: Flow<A2dpDeviceEvent> = callbackFlow {
        trySend(A2dpDeviceEvent.PlayState(state = PlayState.STATE_NOT_PLAYING))
        val playStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
                val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothAdapter.ERROR)

                if (device == null) {
                    trySend(
                        A2dpDeviceEvent.Error(
                            msg = "received null device, ignoring...",
                            `throw` = DeviceNotReceivedException()
                        )
                    )
                } else if (state == BluetoothAdapter.ERROR) {
                    trySend(
                        A2dpDeviceEvent.Error(
                            msg = "received null play state, ignoring...",
                            `throw` = A2dpDevicePlayStateNotReceivedException()
                        )
                    )
                } else {
                    trySend(A2dpDeviceEvent.PlayState(state = state.playStateFromInt()))
                }
            }
        }
        ctx.registerReceiver(
            playStateReceiver,
            IntentFilter(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)
        )

        awaitClose { ctx.unregisterReceiver(playStateReceiver) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val connectionState: Flow<A2dpDeviceEvent> = callbackFlow {
        trySend(A2dpDeviceEvent.ConnectionState(state = ConnectionState.STATE_DISCONNECTED))
        val connectionStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)

                if (device == null) {
                    trySend(
                        A2dpDeviceEvent.Error(
                            msg = "received null device, ignoring...",
                            `throw` = DeviceNotReceivedException()
                        )
                    )
                } else if (state == BluetoothDevice.ERROR) {
                    trySend(
                        A2dpDeviceEvent.Error(
                            msg = "received null connection state, ignoring...",
                            `throw` = A2dpDeviceConnectionStateNotReceivedException()
                        )
                    )
                } else {
                    trySend(A2dpDeviceEvent.ConnectionState(state = state.connectionStateFromInt()))
                }
            }
        }
    }

    enum class PlayState(val num: Int) {
        STATE_PLAYING(10),
        STATE_NOT_PLAYING(11);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.playStateFromInt(): PlayState = lookup[this] ?: STATE_NOT_PLAYING
            fun PlayState.playStateToInt(): Int = this.num
        }
    }
}