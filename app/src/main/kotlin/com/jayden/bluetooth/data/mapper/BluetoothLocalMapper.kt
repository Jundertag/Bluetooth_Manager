package com.jayden.bluetooth.data.mapper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothStatusCodes
import com.jayden.bluetooth.data.model.BluetoothCodes
import com.jayden.bluetooth.data.model.BluetoothLocalModel
import com.jayden.bluetooth.data.model.BluetoothLocalScanMode
import com.jayden.bluetooth.data.model.BluetoothLocalState

object BluetoothLocalMapper {
    fun BluetoothAdapter.toSource(): BluetoothLocalModel {
        return BluetoothLocalModel(
            name = name,
            leMaxAdvertisingDataLength = leMaximumAdvertisingDataLength,
            discovering = isDiscovering,
            state = state.stateToSource(),
            le2MPhySupport = isLe2MPhySupported,
            leAudioBroadcastAssistSupport = isLeAudioBroadcastAssistantSupported.codesToSource(),
            leAudioBroadcastSourceSupport = isLeAudioBroadcastSourceSupported.codesToSource(),
            leAudioSupport = isLeAudioSupported.codesToSource(),
            leCodedPhySupport = isLeCodedPhySupported,
            leExtendedAdvertisingSupport = isLeExtendedAdvertisingSupported,
            lePeriodicAdvertisingSupport = isLePeriodicAdvertisingSupported,
            multipleAdvertisementSupport = isMultipleAdvertisementSupported,
            offloadedFilteringSupport = isOffloadedFilteringSupported,
            offloadedScanBatchingSupport = isOffloadedScanBatchingSupported,
        )
    }

    fun Int.codesToSource(): BluetoothCodes {
        return when (this) {
            BluetoothStatusCodes.ERROR_BLUETOOTH_NOT_ALLOWED -> BluetoothCodes.ERROR_BLUETOOTH_NOT_ALLOWED
            BluetoothStatusCodes.ERROR_BLUETOOTH_NOT_ENABLED -> BluetoothCodes.ERROR_BLUETOOTH_NOT_ENABLED
            BluetoothStatusCodes.ERROR_DEVICE_NOT_BONDED -> BluetoothCodes.ERROR_DEVICE_NOT_BONDED
            BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED -> BluetoothCodes.ERROR_GATT_WRITE_NOT_ALLOWED
            BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY -> BluetoothCodes.ERROR_GATT_WRITE_REQUEST_BUSY
            BluetoothStatusCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION -> BluetoothCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION
            BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND -> BluetoothCodes.ERROR_PROFILE_SERVICE_NOT_BOUND
            BluetoothStatusCodes.ERROR_UNKNOWN -> BluetoothCodes.ERROR_UNKNOWN
            BluetoothStatusCodes.FEATURE_NOT_CONFIGURED -> BluetoothCodes.FEATURE_NOT_CONFIGURED
            BluetoothStatusCodes.FEATURE_NOT_SUPPORTED -> BluetoothCodes.FEATURE_NOT_SUPPORTED
            BluetoothStatusCodes.FEATURE_SUPPORTED -> BluetoothCodes.FEATURE_SUPPORTED
            BluetoothStatusCodes.SUCCESS -> BluetoothCodes.SUCCESS
            else -> BluetoothCodes.ERROR_UNKNOWN
        }
    }

    fun Int.stateToSource(): BluetoothLocalState {
        return when (this) {
            BluetoothAdapter.STATE_OFF -> BluetoothLocalState.STATE_OFF
            BluetoothAdapter.STATE_TURNING_ON -> BluetoothLocalState.STATE_TURNING_ON
            BluetoothAdapter.STATE_ON -> BluetoothLocalState.STATE_ON
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothLocalState.STATE_TURNING_OFF
            else -> BluetoothLocalState.STATE_OFF
        }
    }

    fun Int.scanModeToSource(): BluetoothLocalScanMode {
        return when (this) {
            BluetoothAdapter.SCAN_MODE_NONE -> BluetoothLocalScanMode.SCAN_MODE_NONE
            BluetoothAdapter.SCAN_MODE_CONNECTABLE -> BluetoothLocalScanMode.SCAN_MODE_CONNECTABLE
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> BluetoothLocalScanMode.SCAN_MODE_CONNECTABLE_DISCOVERABLE
            else -> BluetoothLocalScanMode.SCAN_MODE_NONE
        }
    }
}