package com.jayden.bluetooth.data.device

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import com.jayden.bluetooth.data.adapter.HeadsetProfile
import com.jayden.bluetooth.data.device.DeviceEvent.HeadsetDeviceEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HeadsetDeviceCompat(
    private val device: BluetoothDevice,
    private val proxy: BluetoothHeadset
) : DeviceCompat(device) {

    val audioState: Flow<HeadsetDeviceEvent> = callbackFlow {
        if (proxy.isAudioConnected(device)) {
            trySend(HeadsetDeviceEvent.AudioState(AudioState.STATE_AUDIO_CONNECTED))
        }
    }

    enum class AudioState(val num: Int) {
        STATE_AUDIO_DISCONNECTED(10),
        STATE_AUDIO_CONNECTING(11),
        STATE_AUDIO_CONNECTED(12);

        companion object {
            private val lookup = entries.associateBy { it.num}

            fun Int.audioStateFromInt(): AudioState = lookup[this] ?: STATE_AUDIO_DISCONNECTED
            fun AudioState.audioStateToInt(): Int = this.num
        }
    }
}