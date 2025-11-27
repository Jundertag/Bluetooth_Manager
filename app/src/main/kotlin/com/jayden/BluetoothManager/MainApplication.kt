package com.jayden.BluetoothManager

import android.app.Application
import com.jayden.BluetoothManager.context.ContextUtils

class MainApplication : Application() {
    lateinit var applicationGraph: ApplicationGraph

    override fun onCreate() {
        super.onCreate()
        applicationGraph = ApplicationGraph(this.applicationContext)
        ContextUtils.init(this)
    }
}