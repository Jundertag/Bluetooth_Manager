package com.jayden.BluetoothManager.device

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.jayden.BluetoothManager.device.DeviceCompat.BondState.Companion.fromInt
import com.jayden.BluetoothManager.context.ContextUtils
import com.jayden.BluetoothManager.permission.PermissionHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class DeviceCompat(
    private val device: BluetoothDevice
) {
    private val ctx: Context = ContextUtils.getAppContext()

    /**
     * The non-nullable address of the bluetooth device
     */
    val address: String get() = device.address

    /**
     * The alias of the bluetooth device
     *
     * @throws SecurityException if the app doesn't have [Manifest.permission.BLUETOOTH_CONNECT] permission
     */
    val alias: String?
        get() {
            return if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                device.alias
            } else {
                throw SecurityException()
            }
        }

    /**
     * The friendly name of the bluetooth device
     *
     * @throws SecurityException if the app doesn't have [Manifest.permission.BLUETOOTH_CONNECT] permission
     */
    val name: String get() {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return device.name
        } else {
            throw SecurityException()
        }
    }

    /**
     * Suspends until the desired [state] is reached
     *
     * @param state The bond state
     * @param timeoutMs Max suspend duration
     *
     * @throws SecurityException if the app is not given [Manifest.permission.BLUETOOTH_CONNECT] permission
     */
    suspend fun waitForBondState(state: BondState, timeoutMs: Long = 30_000L) {
        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            withTimeoutOrNull(timeoutMs) {
                if (device.bondState.fromInt() == state) {
                    return@withTimeoutOrNull
                }

                suspendCancellableCoroutine {
                    val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val bondState = intent.getIntExtra(
                                BluetoothDevice.EXTRA_BOND_STATE,
                                BluetoothDevice.ERROR
                            )
                            if (bondState.fromInt() == state && it.isActive) {
                                ctx.unregisterReceiver(this)
                                it.resumeWith(Result.success(Unit))
                            }
                        }
                    }

                    ctx.registerReceiver(receiver, filter)

                    it.invokeOnCancellation {
                        try {
                            ctx.unregisterReceiver(receiver)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        } else {
            throw SecurityException()
        }
    }

    fun format(): DeviceCompatUi {
        return DeviceCompatUi(

        )
    }

    enum class BondState(val num: Int) {
        BOND_NONE(10),
        BOND_BONDING(11),
        BOND_BONDED(12);

        companion object {
            private val lookup = entries.associateBy { it.num }
            fun Int.fromInt(): BondState {
                return lookup[this]!!
            }
            fun BondState.toInt(): Int {
                return this.num
            }
        }
    }
}