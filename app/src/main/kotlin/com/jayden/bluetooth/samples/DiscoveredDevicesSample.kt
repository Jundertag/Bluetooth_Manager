package com.jayden.bluetooth.samples

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayden.bluetooth.data.adapter.LocalAdapter
import com.jayden.bluetooth.data.device.DeviceEvent
import kotlinx.coroutines.launch

class DiscoveredDevicesSample(private val adapter: LocalAdapter) : ViewModel() {
    fun start() {
        viewModelScope.launch {
            adapter.discoveredDevices.collect { devices ->
                devices.forEach { event ->
                    when (event) {
                        is DeviceEvent.Found -> {
                            // event.device
                        }
                        is DeviceEvent.Error -> {
                            // Log.e("DiscoveredDevicesSample", event.msg, event.`throw`)
                        }
                    }
                }
            }
        }
    }
}