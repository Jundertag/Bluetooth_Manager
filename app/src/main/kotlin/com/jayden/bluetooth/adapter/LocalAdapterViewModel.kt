package com.jayden.bluetooth.adapter

import android.Manifest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayden.bluetooth.device.DeviceCompat
import com.jayden.bluetooth.adapter.LocalAdapter.State
import com.jayden.bluetooth.adapter.exception.AdapterNotOnException
import com.jayden.bluetooth.permission.PermissionHelper
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext

class LocalAdapterViewModel(
    private val adapter: LocalAdapter
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

        viewModelScope.launch {
            adapter.state.collect { state ->
                _adapterState.update {
                    state == State.STATE_ON
                }
            }
        }

        viewModelScope.launch {
            adapter.name.collect { name ->
                _adapterName.update {
                    name
                }
            }
        }

        viewModelScope.launch {
            adapter.pairedDevices.collect { devices ->
                _boundDevices.update { list ->
                    list.removeAll { !devices.contains(it) }
                    devices.forEach {
                        if (!list.contains(it)) {
                            list.addFirst(it)
                        }
                    }
                    list
                }
            }
        }
    }

    /**
     * Proxy method, start discovery process on the bluetooth adapter
     *
     * @return true if [Manifest.permission.BLUETOOTH_SCAN] permission was granted and therefore discovery has started, false otherwise.
     */
    fun startDiscovery(): Boolean {
        return if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            @Suppress("MissingPermission")
            adapter.startDiscovery()
            true
        } else {
            false
        }
    }

    companion object {
        private const val TAG = "LocalAdapterViewModel"
    }
}