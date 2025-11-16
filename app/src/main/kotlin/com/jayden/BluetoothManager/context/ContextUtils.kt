package com.jayden.BluetoothManager.context

import android.content.Context

object ContextUtils {
    lateinit var context: Context

    fun getAppContext(): Context {
        return context
    }
}