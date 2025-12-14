package com.jayden.bluetooth.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import com.jayden.bluetooth.utils.PermissionHelper.ProtectionLevel.Companion.fromInt
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@SuppressLint("StaticFieldLeak")
object PermissionHelper {
    private val ctx: Context = ContextUtils.getAppContext()

    private var shouldShowRequestPermission: MutableMap<String, Boolean> = mutableMapOf()

    fun isGrantedPermission(permission: String): Boolean {
        return ctx.checkSelfPermission(permission) == GrantState.GRANTED.num
    }

    fun isGrantedPermissions(permissions: Array<String>): Boolean {
        val results = mutableMapOf<String, Boolean>()
        for (perm in permissions) {
            results[perm] = ctx.checkSelfPermission(perm) == GrantState.GRANTED.num
        }
        return !results.values.contains(false)
    }

    fun getPermProtection(permission: String): ProtectionLevel {
        return ctx.packageManager.getPermissionInfo(permission, 0).protection.fromInt()
    }

    fun shouldRequestPermissions(permissions: Array<String>): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        permissions.forEach { permission ->
            result.getOrPut(permission) { false }
        }
        return result
    }

    fun shouldRequestPermission(permission: String): Boolean {
        return shouldRequestPermissions(arrayOf(permission)).containsValue(true)
    }

    suspend fun requestPermissions(activity: ActivityResultCaller, permissions: Array<String>): Map<String, Boolean> {
        return suspendCancellableCoroutine { coroutine ->
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                if (coroutine.isActive) {
                    result.forEach { (perm, granted) ->
                        if (shouldShowRequestPermission.getOrPut(perm) { false }) {
                            shouldShowRequestPermission.putIfAbsent(perm, false)
                        }
                    }
                    coroutine.resume(result)
                }
            }

            coroutine.invokeOnCancellation {
                // automatic clean up
            }

            launcher.launch(permissions)
        }
    }

    /**
     * run [block] only when [permissions] are granted
     *
     * @return result of [block]
     * @throws SecurityException if any of [permissions] aren't granted
     */
    suspend inline fun <T> runIfPermissionsGranted(permissions: Array<String>, crossinline block: suspend () -> T): T {
        if (!isGrantedPermissions(permissions)) {
            return block.invoke()
        } else {
            throw SecurityException()
        }
    }

    /**
     * run [block] only when [permissions] are granted
     *
     * @return null when any of [permissions] aren't granted
     */
    suspend inline fun <T> runIfPermissionsGrantedOrNull(permissions: Array<String>, crossinline block: suspend () -> T): T? {
        return try {
            runIfPermissionsGranted(permissions, block)
        } catch (_: SecurityException) {
            null
        }
    }

    /**
     * run [block] only when [permissions] are granted, otherwise run the [else] block
     */
    suspend inline fun <T> runIfPermissionsGrantedOrElse(permissions: Array<String>, crossinline block: suspend () -> T, crossinline `else`: suspend () -> T): T {
        return try {
            runIfPermissionsGranted(permissions, block)
        } catch (_: SecurityException) {
            withContext(NonCancellable) { `else`.invoke() }
        }
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