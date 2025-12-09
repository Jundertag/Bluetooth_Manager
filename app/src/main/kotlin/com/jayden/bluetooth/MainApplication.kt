package com.jayden.bluetooth

import android.app.Application
import com.jayden.bluetooth.utils.ContextUtils

class MainApplication : Application() {
    lateinit var applicationGraph: ApplicationGraph

    override fun onCreate() {
        super.onCreate()
        applicationGraph = ApplicationGraph(this.applicationContext)
        ContextUtils.init(this)
    }
}