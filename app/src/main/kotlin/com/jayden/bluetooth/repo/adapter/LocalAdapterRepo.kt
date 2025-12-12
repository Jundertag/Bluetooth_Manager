package com.jayden.bluetooth.repo.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import com.jayden.bluetooth.data.adapter.LocalAdapter
import com.jayden.bluetooth.data.adapter.Profile
import com.jayden.bluetooth.utils.ContextUtils
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.flow.distinctUntilChanged

class LocalAdapterRepo(
    val adapter: LocalAdapter
) {
    private val ctx: Context = ContextUtils.getAppContext()

    val nameFlow = adapter.name.distinctUntilChanged()
    val stateFlow = adapter.state.distinctUntilChanged()
    val pairedDevicesFlow = adapter.pairedDevices.distinctUntilChanged()
    val discoveredDevicesFlow = adapter.discoveredDevices.distinctUntilChanged()
    val discoveringFlow = adapter.discovering.distinctUntilChanged()

    suspend inline fun <reified T : Profile> withProfile(block: T.() -> Unit) {
        adapter.withProfile<T>(block)
    }

    /**
     * proxy method to start discovery on [LocalAdapter]
     *
     * @return true when method called, false when there's no BLUETOOTH_SCAN permission.
     */
    fun startDiscovery(): Boolean {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            @SuppressLint("MissingPermission")
            adapter.startDiscovery()
            return true
        } else {
            return false
        }
    }

    /**
     * proxy method to stop discovery on [LocalAdapter]
     *
     * @return true when method called, false when there's no BLUETOOTH_SCAN permission.
     */
    fun stopDiscovery(): Boolean {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            @SuppressLint("MissingPermission")
            adapter.stopDiscovery()
            return true
        } else {
            return false
        }
    }
}