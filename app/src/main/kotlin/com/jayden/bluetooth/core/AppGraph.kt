package com.jayden.bluetooth.core

import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.res.Resources

class AppGraph(application: Application) {
    private val appContext: Context = application.applicationContext
    val bluetoothManager: BluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    val notificationManager: NotificationManager = appContext.getSystemService(NotificationManager::class.java)
    val resources: Resources = appContext.resources
}