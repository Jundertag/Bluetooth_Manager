package com.jayden.bluetooth.core.app

import android.app.Application
import com.jayden.bluetooth.core.dependency.DependencyResolver

class Application : Application() {
    lateinit var dependencyResolver: DependencyResolver

    override fun onCreate() {
        super.onCreate()
        dependencyResolver = DependencyResolver(this.applicationContext)
    }
}