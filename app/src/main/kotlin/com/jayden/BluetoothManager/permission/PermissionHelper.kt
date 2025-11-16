package com.jayden.BluetoothManager.permission

import android.content.Context
import com.jayden.BluetoothManager.context.ContextUtils
import com.jayden.BluetoothManager.permission.PermissionHelper.ProtectionLevel.Companion.fromInt

object PermissionHelper {
    private val ctx: Context = ContextUtils.getAppContext()

    fun isGrantedPermission(permission: String): Boolean {
        return ctx.checkSelfPermission(permission) == GrantState.GRANTED.num
    }

    fun getPermProtection(permission: String): ProtectionLevel {
        return ctx.packageManager.getPermissionInfo(permission, 0).protection.fromInt()
    }

    enum class GrantState(val num: Int) {
        DENIED(-1),
        GRANTED(0);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.fromInt(): GrantState = lookup[this]!!
            fun GrantState.toInt(): Int = this.num
        }
    }

    enum class ProtectionLevel(val num: Int) {
        NORMAL(0),
        DANGEROUS(1),
        SIGNATURE(2),
        SIGNATURE_OR_INTERNAL(3),
        INTERNAL(4);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.fromInt(): ProtectionLevel = lookup[this]!!
            fun ProtectionLevel.toInt(): Int = this.num
        }
    }
}