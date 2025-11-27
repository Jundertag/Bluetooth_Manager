package com.jayden.BluetoothManager.context

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextUtils {
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getAppContext(): Context {
        return context
    }
}