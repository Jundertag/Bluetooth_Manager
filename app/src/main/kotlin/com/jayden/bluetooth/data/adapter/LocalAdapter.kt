package com.jayden.bluetooth.data.adapter

import android.Manifest
import android.annotation.SuppressLint
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
import com.jayden.bluetooth.data.adapter.LocalAdapter.ProfileProxy.Companion.proxyFromInt
import com.jayden.bluetooth.data.adapter.LocalAdapter.ProfileProxy.Companion.proxyToInt
import com.jayden.bluetooth.data.adapter.LocalAdapter.State.Companion.stateFromInt
import com.jayden.bluetooth.data.device.A2dpDeviceCompat
import com.jayden.bluetooth.data.device.DeviceCompat
import com.jayden.bluetooth.data.device.DeviceCompat.Transport.Companion.transportFromInt
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.data.device.exception.DeviceNotReceivedException
import com.jayden.bluetooth.utils.ContextUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocalAdapter(
    manager: BluetoothManager
) {
    val adapter: BluetoothAdapter = manager.adapter

    private val ctx: Context = ContextUtils.getAppContext()

    /**
     * Current state of the local adapter as a [Flow]
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
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val pairedDevices: Flow<List<DeviceCompat>> = callbackFlow {
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
                trySend(devicesList.toList())
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
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val name: Flow<String> = callbackFlow {
        @Suppress("MissingPermission")
        trySend(adapter.name)
        val nameReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                trySend(intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME) ?: "")
            }
        }
        ctx.registerReceiver(nameReceiver, IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED))

        awaitClose { ctx.unregisterReceiver(nameReceiver) }
    }

    /**
     * With the context of the profile, perform the specified operations...
     *
     * but wait! it's suspending!!!
     *
     * @throws IllegalStateException if profile type is not a parent subtype of [Profile]
     */
    suspend inline fun <reified T : Profile> withProfile(block: T.() -> Unit) {
        val (proxyType, profile) = when (T::class) {
            A2dpProfile::class -> {
                ProfileProxy.A2DP to (getProfile(ProfileProxy.A2DP) as A2dpProfile)
            }
            HeadsetProfile::class -> {
                ProfileProxy.HEADSET to (getProfile(ProfileProxy.HEADSET) as HeadsetProfile)
            }
            else -> error("profile ${T::class.simpleName} is not supported on android")
        }

        try {
            (profile as T).block()
        } finally {
            adapter.closeProfileProxy(proxyType.proxyToInt(), profile.rawProfile)
        }
    }

    suspend fun getProfile(type: ProfileProxy): Profile = suspendCancellableCoroutine {
        val profileCallback = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(
                profile: Int,
                proxy: BluetoothProfile?
            ) {
                if (profile == type.proxyToInt() && proxy != null && it.isActive) {
                    val profileProxy: Profile = when (profile.proxyFromInt()) {
                        ProfileProxy.A2DP -> {
                            A2dpProfile(proxy as BluetoothA2dp)
                        }
                        ProfileProxy.HEADSET -> {
                            HeadsetProfile(proxy as BluetoothHeadset)
                        }
                    }
                    it.resume(profileProxy)
                }
            }

            override fun onServiceDisconnected(profile: Int) {

            }

        }
        if (!adapter.getProfileProxy(ctx, profileCallback, type.proxyToInt())) {
            it.cancel(null)
        }
    }

    /**
     * [Flow] of discovered devices wrapped in a set of [DeviceEvent].
     *
     * @sample com.jayden.bluetooth.samples.DiscoveredDevicesSample
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    val discoveredDevices: Flow<Set<DeviceEvent>> = callbackFlow {
        val devices: MutableSet<DeviceCompat> = mutableSetOf()
        val deviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
                val rssi = intent.getIntExtra(BluetoothDevice.EXTRA_RSSI, -1)

                if (device == null) {
                    trySend(setOf(DeviceEvent.Error(msg = "received null device, ignoring...")))
                } else {
                    devices.add(DeviceCompat(device))
                    trySend(devices.map { device -> DeviceEvent.Found(device) }.toSet())
                }

            }
        }

        ctx.registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        awaitClose { ctx.unregisterReceiver(deviceReceiver) }
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
            fun Int.stateFromInt(): State = lookup[this] ?: State.STATE_OFF
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
}