package com.jayden.bluetooth.data.adapter

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.jayden.bluetooth.data.adapter.LocalAdapter.ConnectionState.Companion.connectionStateFromInt
import com.jayden.bluetooth.data.adapter.LocalAdapter.PlayState.Companion.playStateFromInt
import com.jayden.bluetooth.data.adapter.LocalAdapter.State.Companion.stateFromInt
import com.jayden.bluetooth.device.DeviceCompat
import com.jayden.bluetooth.device.DeviceCompat.Transport.Companion.transportFromInt
import com.jayden.bluetooth.device.exception.DeviceNotReceivedException
import com.jayden.bluetooth.utils.ContextUtils
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocalAdapter(
    manager: BluetoothManager
) {
    private val adapter = manager.adapter

    private val ctx: Context = ContextUtils.getAppContext()

    /**
     * Current state of the local adapter as a [kotlinx.coroutines.flow.Flow]
     */
    val state: Flow<State> = callbackFlow {
        trySend(adapter.state.stateFromInt())
        val stateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val state =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        ?: BluetoothAdapter.ERROR

                trySend(state.stateFromInt())
            }
        }
        ctx.registerReceiver(stateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        awaitClose {
            ctx.unregisterReceiver(stateReceiver)
        }
    }


    /**
     * Represents a [Flow] of the devices that are paired to this adapter (including disconnected devices)
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val pairedDevices: Flow<MutableList<DeviceCompat>> = callbackFlow {
        val devicesList = mutableListOf<DeviceCompat>()
        adapter.bondedDevices.forEach { device ->
            devicesList.add(DeviceCompat(device))
        }
        trySend(devicesList)
        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val device =
                    intent?.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )!!
                val rssi = intent.getIntExtra(BluetoothDevice.EXTRA_RSSI, -1)

                devicesList.add(DeviceCompat(device).also {
                    if (rssi != -1) {
                        it.rssi = rssi
                    }
                })
                trySend(devicesList)
            }
        }
        ctx.registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        awaitClose { ctx.unregisterReceiver(deviceReceiver) }
    }

    /**
     * Whether the adapter is actively searching for devices that are advertising, wrapped in a [Flow]
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    val discovering: Flow<Boolean> = callbackFlow {
        @Suppress("MissingPermission")
        trySend(adapter.isDiscovering)
        val discoveryStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        trySend(true)
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        trySend(false)
                    }

                    else -> {
                        trySend(false)
                    }
                }
            }
        }
        ctx.registerReceiver(discoveryStateReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        })

        awaitClose { ctx.unregisterReceiver(discoveryStateReceiver) }
    }

    /**
     * The adapter's name returned as a [Flow], will be empty if adapter is off, or display a message specifying missing permissions.
     */
    val name: Flow<String> = callbackFlow {
        val nameReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                trySend(intent?.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME) ?: "")
            }
        }
        ctx.registerReceiver(nameReceiver, IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED))

        awaitClose { ctx.unregisterReceiver(nameReceiver) }
    }

    private var _a2dpProfile: A2dpProfile? = null
    val a2dpProfile get() = _a2dpProfile!!

    private var _headsetProfile: HeadsetProfile? = null
    val headsetProfile get() = _headsetProfile!!

    val discoveredDevices: Flow<MutableList<DeviceCompat>> = callbackFlow {
        val devices: MutableList<DeviceCompat> = mutableListOf()
        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val device = intent!!.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )!!
                val rssi = intent.getIntExtra(BluetoothDevice.EXTRA_RSSI, BluetoothDevice.ERROR)
                val compat = DeviceCompat(device).also {
                    if (rssi != BluetoothDevice.ERROR) {
                        it.rssi = rssi
                    }
                }
                if (!devices.contains(compat)) {
                    devices.add(compat)
                }
                trySend(devices)
            }
        }

        ctx.registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        awaitClose { ctx.unregisterReceiver(deviceReceiver) }
    }


    /**
     * loads the [profile] into memory which can be accessed with dot syntax
     *
     * will block until the bluetooth profile is loaded into memory
     *
     * @param profile one of [ProfileProxy.HEADSET] or [ProfileProxy.A2DP]
     *
     * @return true when the profile loads, false if there was an API error.
     */
    suspend fun loadProfileProxy(profile: ProfileProxy): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val profileReceiver = object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(
                    profile: Int,
                    proxy: BluetoothProfile?
                ) {
                    when (profile) {
                        BluetoothProfile.A2DP -> {
                            _a2dpProfile = A2dpProfile(proxy as BluetoothA2dp)
                            continuation.resume(true)
                        }

                        BluetoothProfile.HEADSET -> {
                            _headsetProfile = HeadsetProfile(proxy as BluetoothHeadset)
                            continuation.resume(true)
                        }
                    }
                }

                override fun onServiceDisconnected(profile: Int) {}
            }
            if (continuation.isActive) {
                when (profile) {
                    ProfileProxy.A2DP -> {
                        if (!adapter.getProfileProxy(ctx, profileReceiver, BluetoothProfile.A2DP)) {
                            continuation.resume(false)
                        }
                    }

                    ProfileProxy.HEADSET -> {
                        if (!adapter.getProfileProxy(
                                ctx,
                                profileReceiver,
                                BluetoothProfile.HEADSET
                            )
                        ) {
                            continuation.resume(false)
                        }
                    }
                }
            }

            continuation.invokeOnCancellation {
                // automatic clean up
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery() {
        adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        adapter.cancelDiscovery()
    }

    enum class State(val num: Int) {
        STATE_OFF(10),
        STATE_TURNING_ON(11),
        STATE_ON(12),
        STATE_TURNING_OFF(13);

        companion object {
            private val lookup = entries.associateBy { it.num }
            fun Int.stateFromInt(): State = lookup[this]!!
            fun State.stateToInt(): Int = this.num
        }
    }

    enum class ProfileProxy(val num: Int) {
        HEADSET(1),
        A2DP(2);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.proxyFromInt(): ProfileProxy = lookup[this]!!
            fun ProfileProxy.proxyToInt(): Int = this.num
        }
    }

    enum class ConnectionState(val num: Int) {
        STATE_DISCONNECTED(0),
        STATE_CONNECTING(1),
        STATE_CONNECTED(2),
        STATE_DISCONNECTING(3);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.connectionStateFromInt(): ConnectionState = lookup[this]!!
            fun ConnectionState.connectionStateToInt(): Int = this.num
        }
    }

    enum class PlayState(val num: Int) {
        STATE_PLAYING(10),
        STATE_NOT_PLAYING(11);

        companion object {
            private val lookup = entries.associateBy { it.num }

            fun Int.playStateFromInt(): PlayState = lookup[this]!!
            fun PlayState.playStateToInt(): Int = this.num
        }
    }

    @Suppress("MissingPermission")
    inner class A2dpProfile(proxy: BluetoothA2dp) {
        private val device = proxy.getDevicesMatchingConnectionStates(intArrayOf(
            BluetoothA2dp.STATE_CONNECTED,
            BluetoothA2dp.STATE_CONNECTING,
            BluetoothA2dp.STATE_DISCONNECTING)
        ).firstOrNull()

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        val connectionState: Flow<ConnectionState> = callbackFlow {
            if (device == null) {
                trySend(ConnectionState.STATE_DISCONNECTED)
            } else {
                trySend(proxy.getConnectionState(device).connectionStateFromInt())
            }
            val connectionCallback = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val state = intent!!.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)
                    if (state != BluetoothDevice.ERROR) {
                        trySend(state.connectionStateFromInt())
                    }
                }
            }
            ctx.registerReceiver(connectionCallback, IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED))

            awaitClose { ctx.unregisterReceiver(connectionCallback) }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        val playState: Flow<PlayState> = callbackFlow {
            if (device == null) {
                trySend(PlayState.STATE_NOT_PLAYING)
            } else {
                val playing = proxy.isA2dpPlaying(device)
                if (playing) {
                    trySend(PlayState.STATE_PLAYING)
                } else {
                    trySend(PlayState.STATE_NOT_PLAYING)
                }
            }

            val playStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val state = intent!!.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothDevice.ERROR)
                    if (state != BluetoothDevice.ERROR) {
                        trySend(state.playStateFromInt())
                    }
                }
            }
            ctx.registerReceiver(playStateReceiver, IntentFilter(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED))

            awaitClose { ctx.unregisterReceiver(playStateReceiver) }
        }
    }

    @Suppress("MissingPermission")
    inner class HeadsetProfile(proxy: BluetoothHeadset) {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        val connectedDevices: Flow<MutableList<DeviceCompat>> = callbackFlow {
            val devices = mutableListOf<DeviceCompat>()
            proxy.connectedDevices.forEach { device ->
                devices.add(DeviceCompat(device))
            }
            trySend(devices)

            val deviceReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_ACL_CONNECTED -> {
                            val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                            val transport = intent.getIntExtra(BluetoothDevice.EXTRA_TRANSPORT, BluetoothDevice.ERROR)

                            if (device == null) {
                                throw DeviceNotReceivedException()
                            } else {
                                devices.add(DeviceCompat(device).also {
                                    if (transport != BluetoothDevice.ERROR) {
                                        it.transport = transport.transportFromInt()
                                    }
                                })
                            }
                        }
                        BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                            val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

                            if (device == null) {
                                throw DeviceNotReceivedException()
                            } else {
                                val removable = devices.takeIf { devices ->
                                    var result = false
                                    devices.forEach {
                                        if (it.rawDevice == device) {
                                            result = true
                                        }
                                    }
                                    result
                                }?.firstOrNull()
                                if (removable != null) {
                                    devices.remove(removable)
                                }
                            }
                        }
                    }
                }
            }
            ctx.registerReceiver(deviceReceiver, IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            })

            awaitClose { ctx.unregisterReceiver(deviceReceiver) }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        val discoveredDevices: Flow<MutableList<DeviceCompat>> = callbackFlow {

        }
    }
}