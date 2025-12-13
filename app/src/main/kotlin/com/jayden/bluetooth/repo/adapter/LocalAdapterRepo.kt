package com.jayden.bluetooth.repo.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.jayden.bluetooth.data.adapter.LocalAdapter
import com.jayden.bluetooth.data.adapter.Profile
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.repo.devices.DeviceRepo
import com.jayden.bluetooth.utils.ContextUtils
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class LocalAdapterRepo(
    val adapter: LocalAdapter
) {
    private val ctx: Context = ContextUtils.getAppContext()

    val nameFlow = adapter.name.distinctUntilChanged()

    val stateFlow = adapter.state.distinctUntilChanged()

    val pairedDevicesFlow = adapter.pairedDevices.distinctUntilChanged()
        .map {
            var transform = listOf<DeviceRepo>()
            it.forEach { device -> DeviceRepo(device) }
            transform
        }

    val discoveredDevicesFlow =
        adapter.discoveredDevices.distinctUntilChanged()
            .filterIsInstance<Set<DeviceEvent.Found>>()
            .map {
                var transform = setOf<DeviceRepo>()
                it.forEach { event -> DeviceRepo(event.device) }
                transform
            }

    val discoveringFlow = adapter.discovering.distinctUntilChanged()

    suspend inline fun <reified T : Profile, R> withProfile(block: T.() -> R): R {
        return adapter.withProfile<T, R>(block)
    }

    /**
     * ensure the device is discovering before running [block] and returning the result.
     *
     * @throws SecurityException missing BLUETOOTH_SCAN && ACCESS_FINE_LOCATION permissions.
     * @throws TimeoutCancellationException if timeout exceeds.
     */
    suspend fun <T> withDiscovering(timeout: Long = 5_000, block: suspend () -> T): T {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            @SuppressLint("MissingPermission")
            adapter.startDiscovery()
            withTimeout(timeout) {
                adapter.discovering.distinctUntilChanged().first { it }
            }

            return try {
                block()
            } finally {
                withContext(NonCancellable) {
                    adapter.stopDiscovery()
                }
            }
        } else {
            throw SecurityException()
        }
    }

    /**
     * ensure the device is discovering before running [block] and returning the result.
     *
     * @return null if missing BLUETOOTH_SCAN && ACCESS_FINE_LOCATION perms, or when [timeout] is exceeded.
     */
    suspend fun <T> withDiscoveringOrNull(timeout: Long = 5_000, block: suspend () -> T): T? {
        return try {
            withDiscovering(timeout, block)
        } catch (_: SecurityException) {
            null
        } catch (_: TimeoutCancellationException) {
            null
        }
    }
}