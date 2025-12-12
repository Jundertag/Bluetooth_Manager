package com.jayden.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import com.jayden.bluetooth.data.adapter.LocalAdapter
import com.jayden.bluetooth.utils.ContextUtils
import com.jayden.bluetooth.core.settings.SettingsDataStore
import com.jayden.bluetooth.core.settings.dataStore
import com.jayden.bluetooth.repo.adapter.LocalAdapterRepo

class ApplicationGraph(
    private val appContext: Context
) {
    init {
        ContextUtils.context = appContext
    }
    val manager: BluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    val localAdapter: LocalAdapter = LocalAdapter(manager)
    val localAdapterRepo: LocalAdapterRepo = LocalAdapterRepo(localAdapter)

    private val appSettingsDataStore = appContext.dataStore

    val settingsDataStore = SettingsDataStore(appSettingsDataStore)
}