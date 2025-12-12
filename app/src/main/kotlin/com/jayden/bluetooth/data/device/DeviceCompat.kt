package com.jayden.bluetooth.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.core.exception.PermissionException
import com.jayden.bluetooth.data.adapter.exception.AdapterNotOnException
import com.jayden.bluetooth.data.device.DeviceCompat.BondState.Companion.fromInt
import com.jayden.bluetooth.data.device.DeviceCompat.DeviceType.Companion.deviceTypeFromInt
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
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
    protected val ctx: Context = ContextUtils.getAppContext()

    /**
     * The non-nullable address of the bluetooth device
     */
    val address: String get() = device.address

    /**
     * The alias of the bluetooth device
     *
     * @see [com.jayden.bluetooth.data.adapter.LocalAdapter.discoveredDevices] for an example usage of the API
     *
     * @return the friendly alias of the device
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val alias: Flow<DeviceEvent> = callbackFlow {
        val alias = device.alias
        if (alias == null) {

        } else {
            if (device.alias != device.name) {
                trySend(DeviceEvent.Alias(alias = device.alias))
            } else {
                trySend(DeviceEvent.Alias(alias = null))
            }
        }

        val aliasReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    trySend(DeviceEvent.Error(msg = "permission BLUETOOTH_CONNECT is not granted", `throw` = PermissionException()))
                } else {
                    val device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                    if (device == this@DeviceCompat.device) {
                        val alias = device.alias
                        if (alias == null) {
                            trySend(DeviceEvent.Error(msg = "android `getService()` returned null", `throw` = DeviceServiceException()))
                        } else if (device.alias == device.name) {
                            trySend(DeviceEvent.Alias(alias = null))
                        } else {
                            trySend(DeviceEvent.Alias(alias = device.alias))
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
     * @see [com.jayden.bluetooth.data.adapter.LocalAdapter.discoveredDevices] for an example usage of the API
     *
     * @return the friendly name of the device
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val name: Flow<DeviceEvent> = callbackFlow {
        if (!PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            trySend(
                DeviceEvent.Error(
                    msg = "permission BLUETOOTH_CONNECT is not granted",
                    `throw` = PermissionException()
                )
            )
        } else if (device.name == null) {
            trySend(DeviceEvent.Name(name = device.name))
        }

        val nameReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    trySend(DeviceEvent.Error(msg = "permission BLUETOOTH_CONNECT is not granted", `throw` = PermissionException()))
                } else {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

                    when (device) {
                        null -> {
                            trySend(
                                DeviceEvent.Error(
                                    msg = "received null device, ignoring...",
                                    `throw` = DeviceNotReceivedException()
                                )
                            )
                        }
                        this@DeviceCompat.device -> {
                            trySend(DeviceEvent.Name(name = device.name))
                        }
                        else -> {
                            Log.v(TAG, "received unrelated device ${device.name ?: "<no-name>"}")
                        }
                    }
                }
            }
        }
        ctx.registerReceiver(nameReceiver, IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED))

        awaitClose { ctx.unregisterReceiver(nameReceiver) }
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

    enum class ConnectionState(val num: Int) {
        STATE_DISCONNECTED(0),
        STATE_CONNECTING(1),
        STATE_CONNECTED(2),
        STATE_DISCONNECTING(3);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.connectionStateFromInt(): ConnectionState = lookup[this] ?: STATE_DISCONNECTED
            fun ConnectionState.connectionStateToInt(): Int = this.num
        }
    }

    companion object {
        private const val TAG = "DeviceCompat"
    }
}