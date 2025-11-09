package com.jayden.bluetooth.core.app

import android.app.Application
import com.jayden.bluetooth.core.AppGraph

class Application : Application() {
    lateinit var appGraph: AppGraph

    override fun onCreate() {
        super.onCreate()
        appGraph = AppGraph(this)
    }
}