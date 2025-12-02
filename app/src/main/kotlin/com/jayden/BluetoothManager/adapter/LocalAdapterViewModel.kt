package com.jayden.BluetoothManager.adapter

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.jayden.BluetoothManager.device.DeviceCompat
import com.jayden.BluetoothManager.adapter.LocalAdapter.State
import com.jayden.BluetoothManager.adapter.exception.AdapterNotOnException
import com.jayden.BluetoothManager.permission.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalAdapterViewModel(
    private val adapter: LocalAdapter
) : ViewModel() {
    private val _boundDevices = MutableStateFlow(mutableListOf<DeviceCompat>())
    val boundDevices = _boundDevices.asStateFlow()

    val requestEnableAction = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

    private val _adapterName = MutableStateFlow("")
    val adapterName = _adapterName.asStateFlow()

    /**
     * call when the app should hook into system resources such as bluetooth.
     *
     * should only be called once.
     */
    fun start() {
        Log.d(TAG, "start()")
        if (adapter.state != State.STATE_ON) {
            Log.w(TAG, "bluetooth is not on")
            // stops future code, change if necessary
            throw AdapterNotOnException()
        }
        _boundDevices.value = adapter.pairedDevices.toMutableList()
        _adapterName.update {
            try {
                adapter.name
            } catch (e: SecurityException) {
                Log.w(TAG, "app doesn't have BLUETOOTH_CONNECT permissions", e)
                "<no-bluetooth-connect-perms>"
            }
        }
    }

    /**
     * Proxy method, start discovery process on the bluetooth adapter
     *
     * @return true if [Manifest.permission.BLUETOOTH_SCAN] permission was granted and therefore discovery has started, false otherwise.
     */
    fun startDiscovery(): Boolean {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            @Suppress("MissingPermission")
            adapter.startDiscovery()
            return true
        } else {
            return false
        }
    }

    companion object {
        private const val TAG = "LocalAdapterViewModel"
    }
}