package com.jayden.bluetooth.app.viewmodel.main.pages

import android.Manifest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayden.bluetooth.data.adapter.A2dpProfile
import com.jayden.bluetooth.data.adapter.LocalAdapter
import com.jayden.bluetooth.data.device.DeviceCompat
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.repo.adapter.LocalAdapterRepo
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocalAdapterViewModel(
    private val repo: LocalAdapterRepo
) : ViewModel() {
    private val _boundDevices = MutableStateFlow(mutableListOf<DeviceCompat>())
    val boundDevices = _boundDevices.asStateFlow()

    private val _adapterName = MutableStateFlow("<no-bluetooth-connect-perms>")
    val adapterName = _adapterName.asStateFlow()

    private val _adapterState = MutableStateFlow(false)
    val adapterState = _adapterState.asStateFlow()

    /**
     * call when the app should hook into bluetooth apis.
     *
     * should only be called once.
     */
    fun start() {
        Log.d(TAG, "start()")
        // TODO: repo hooks
    }


    /**
     * Proxy method, start discovery process on the bluetooth adapter
     *
     * @return true if [Manifest.permission.BLUETOOTH_SCAN] permission was granted and therefore discovery has started, false otherwise.
     */
    fun startDiscovery(): Boolean {
        return if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            @Suppress("MissingPermission")
            repo.startDiscovery()
            true
        } else {
            false
        }
    }

    companion object {
        private const val TAG = "LocalAdapterViewModel"
    }
}