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
import com.jayden.bluetooth.repo.devices.DeviceRepo
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer

class LocalAdapterViewModel(
    private val repo: LocalAdapterRepo
) : ViewModel() {
    val boundDevices: StateFlow<List<DeviceRepo>> = repo.pairedDevicesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            listOf()
        )

    val adapterName: StateFlow<String> = repo.nameFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ""
    )

    val adapterState: StateFlow<LocalAdapter.State> = repo.stateFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LocalAdapter.State.STATE_OFF
    )

    companion object {
        private const val TAG = "LocalAdapterViewModel"
    }
}