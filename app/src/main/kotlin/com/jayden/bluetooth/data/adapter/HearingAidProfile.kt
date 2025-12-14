package com.jayden.bluetooth.data.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHearingAid
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.device.DeviceCompat
import com.jayden.bluetooth.data.device.DeviceCompat.ConnectionState.Companion.connectionStateFromInt
import com.jayden.bluetooth.data.device.DeviceEvent.HearingAidDeviceEvent
import com.jayden.bluetooth.data.device.HearingAidDeviceCompat
import com.jayden.bluetooth.data.device.exception.DeviceConnectionStateNotReceivedException
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

open class HearingAidProfile(private val proxy: BluetoothHearingAid) : Profile() {
    override val rawProfile get() = proxy

    /**
     *  All devices matching connection states as a [Flow]
     *
     *  @throws SecurityException if lacking BLUETOOTH_CONNECT permission
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val devicesFlow: Flow<List<HearingAidDeviceEvent>> = callbackFlow {
        var devices: MutableList<HearingAidDeviceEvent> = mutableListOf()
        @SuppressLint("MissingPermission")
        devices = proxy.getDevicesMatchingConnectionStates(intArrayOf(
            BluetoothProfile.STATE_CONNECTED,
            BluetoothProfile.STATE_CONNECTING,
            BluetoothProfile.STATE_DISCONNECTED,
            BluetoothProfile.STATE_DISCONNECTING
        )).map {
            HearingAidDeviceEvent.Found(HearingAidDeviceCompat(it, proxy))
        }.toMutableList()
        trySend(devices)

        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val receivedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                val receivedState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)

                if (receivedDevice == null) {
                    devices.add(HearingAidDeviceEvent.Error(msg = "received null device, ignoring...", `throw` = DeviceNotReceivedException()))
                } else if (receivedState == BluetoothDevice.ERROR) {
                    devices.add(HearingAidDeviceEvent.Error(msg = "received null connection state, ignoring..."))
                } else {
                    var listDevice: BluetoothDevice? = null
                    devices.forEach { event ->
                        when (event) {
                            is HearingAidDeviceEvent.Found -> {
                                listDevice = event.device.rawDevice
                            }
                            else -> {

                            }
                        }
                    }

                    if (listDevice == null) {

                    }
                }
            }
        }
    }
}