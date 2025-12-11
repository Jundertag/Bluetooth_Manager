package com.jayden.bluetooth.data.device

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.adapter.exception.AdapterNotOnException
import com.jayden.bluetooth.data.device.DeviceCompat.BondState.Companion.fromInt
import com.jayden.bluetooth.data.device.DeviceCompat.DeviceType.Companion.deviceTypeFromInt
import com.jayden.bluetooth.data.device.exception.DeviceServiceException
import com.jayden.bluetooth.utils.ContextUtils
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

open class DeviceCompat(
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
     * The backend implementation in [BluetoothDevice] tries to get the service.
     * If null, an error is logged with "BT not enabled.". We try to throw
     * [DeviceServiceException] in this case if we know the adapter is on.
     *
     * @return the friendly alias of the device, or null if there is none
     *
     * @throws AdapterNotOnException if the adapter is not on
     * @throws DeviceServiceException on BluetoothDevice.getService() null error
     * @throws SecurityException not granted [Manifest.permission.BLUETOOTH_CONNECT] permission.
     * Flow will also report null if caught
     */
    val alias: Flow<String?> = callbackFlow {
        val alias = device.alias
        if (alias == null) {
            throw AdapterNotOnException("system error also plausible") // most likely, but system error can sometimes be plausible
        } else {
            if (device.alias != device.name) {
                trySend(device.alias)
            } else {
                trySend(null)
            }
        }

        val aliasReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    trySend(null)
                    throw SecurityException()
                } else {
                    val device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                    if (device == this@DeviceCompat.device) {
                        val alias = device.alias
                        if (alias == null) {
                            throw DeviceServiceException() // realistically should never happen
                        } else {
                            if (device.alias == device.name) {
                                trySend(device.alias)
                            } else {
                                trySend(null)
                            }
                        }
                    }
                }
            }
        }
        ctx.registerReceiver(aliasReceiver, IntentFilter(BluetoothDevice.ACTION_ALIAS_CHANGED))

        awaitClose { ctx.unregisterReceiver(aliasReceiver) }
    }

    /**
     * The friendly name of the bluetooth device
     *
     * @throws SecurityException not granted [Manifest.permission.BLUETOOTH_CONNECT] permission
     */
    val name: Flow<String> = callbackFlow {
        trySend(device.name)

        val nameReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    trySend("<no-permission>")
                    throw SecurityException()
                } else {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    if (device == this@DeviceCompat.device) {
                        val name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME)
                        if (name == null) {
                            trySend("<no-name>")
                        } else {
                            trySend(name)
                        }
                    }
                }
            }
        }
    }

    /**
     * The device type
     *
     * @throws SecurityException not granted [Manifest.permission.BLUETOOTH_CONNECT] permission
     */
    val deviceType: Flow<DeviceType> = callbackFlow {
        if (!PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException()
        } else {
            trySend(device.type.deviceTypeFromInt())
        }
    }

    /**
     * gets the raw [BluetoothDevice] held by this proxy.
     *
     * rarely necessary and unsafe practically. Only use read-only
     */
    val rawDevice get() = this.device

    /**
     * received signal strength indicator
     *
     * only update when data suggests so.
     */
    var rssi: Int? = null

    /**
     * transport used to communicate with the device
     *
     * matches with [BluetoothDevice.getType] except it's the currently connected channel.
     */
    var transport: Transport? = null

    /**
     * Suspends until the desired [state] is reached
     *
     * @param state The bond state
     * @param timeoutMs Max suspend duration
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun waitForBondState(state: BondState, timeoutMs: Long = 30_000L) {
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
    }

    enum class BondState(val num: Int) {
        BOND_NONE(10),
        BOND_BONDING(11),
        BOND_BONDED(12);

        companion object {
            private val lookup = entries.associateBy { it.num }
            fun Int.fromInt(): BondState = lookup[this]!!
            fun BondState.toInt(): Int = this.num
        }
    }

    enum class Transport(val num: Int) {
        TRANSPORT_BREDR(1),
        TRANSPORT_LE(2);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.transportFromInt(): Transport = lookup[this]!!
            fun Transport.transportToInt(): Int = this.num
        }
    }

    enum class DeviceType(val num: Int) {
        DEVICE_TYPE_UNKNOWN(0),
        DEVICE_TYPE_CLASSIC(1),
        DEVICE_TYPE_LE(2);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.deviceTypeFromInt(): DeviceType = lookup[this]!!
            fun DeviceType.deviceTypeToInt(): Int = this.num
        }
    }
}