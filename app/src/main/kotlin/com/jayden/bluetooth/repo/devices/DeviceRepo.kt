package com.jayden.bluetooth.repo.devices

import com.jayden.bluetooth.data.device.DeviceCompat
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.model.DeviceCompatUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

class DeviceRepo(
    private val device: DeviceCompat
) {
    val address: String = device.address
    val alias: Flow<String?> = device.alias.filterIsInstance<DeviceEvent.Alias>().map { it.alias }
    val name: Flow<String> = device.name.filterIsInstance<DeviceEvent.Name>().map { it.name }

    val ui: Flow<DeviceCompatUi> = name.combine(
        alias
    ) { name, alias ->
        DeviceCompatUi(name = name, address = address, alias = alias)
    }
}