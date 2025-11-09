package com.jayden.bluetooth.data.mapper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device
import android.bluetooth.BluetoothClass.Device.Major
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import com.jayden.bluetooth.data.model.BluetoothAddressType
import com.jayden.bluetooth.data.model.BluetoothBondState
import com.jayden.bluetooth.data.model.BluetoothDeviceClass
import com.jayden.bluetooth.data.model.BluetoothDeviceConnectionState
import com.jayden.bluetooth.data.model.BluetoothMinorDeviceClass
import com.jayden.bluetooth.data.model.BluetoothDeviceModel
import com.jayden.bluetooth.data.model.BluetoothDeviceType
import com.jayden.bluetooth.data.model.BluetoothMajorDeviceClass

object BluetoothDeviceMapper {

    fun BluetoothDevice.toSource(connectionState: BluetoothDeviceConnectionState = BluetoothDeviceConnectionState.STATE_DISCONNECTED): BluetoothDeviceModel {
        val addressType = when (addressType) {
            BluetoothDevice.ADDRESS_TYPE_PUBLIC -> BluetoothAddressType.ADDRESS_TYPE_PUBLIC
            BluetoothDevice.ADDRESS_TYPE_RANDOM -> BluetoothAddressType.ADDRESS_TYPE_RANDOM
            BluetoothDevice.ADDRESS_TYPE_ANONYMOUS -> BluetoothAddressType.ADDRESS_TYPE_ANONYMOUS
            BluetoothDevice.ADDRESS_TYPE_UNKNOWN -> BluetoothAddressType.ADDRESS_TYPE_UNKNOWN
            else -> BluetoothAddressType.ADDRESS_TYPE_UNKNOWN
        }

        val bondState: BluetoothBondState = when (bondState) {
            BluetoothDevice.BOND_NONE -> BluetoothBondState.BOND_NONE
            BluetoothDevice.BOND_BONDING -> BluetoothBondState.BOND_BONDING
            BluetoothDevice.BOND_BONDED -> BluetoothBondState.BOND_BONDED
            else -> BluetoothBondState.BOND_NONE
        }

        val type: BluetoothDeviceType = when (type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> BluetoothDeviceType.DEVICE_TYPE_CLASSIC
            BluetoothDevice.DEVICE_TYPE_LE -> BluetoothDeviceType.DEVICE_TYPE_LE
            BluetoothDevice.DEVICE_TYPE_DUAL -> BluetoothDeviceType.DEVICE_TYPE_DUAL
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> BluetoothDeviceType.DEVICE_TYPE_UNKNOWN
            else -> BluetoothDeviceType.DEVICE_TYPE_UNKNOWN
        }

        return BluetoothDeviceModel(
            address = address,
            addressType = addressType,
            alias = alias,
            deviceClass = bluetoothClass.toSource(),
            bondState = bondState,
            connectionState = connectionState,
            name = name,
            type = type,
            uuids = uuids.toSource(),
        )
    }

    fun Array<ParcelUuid>.toSource(): List<String> {
        val result: MutableList<String> = mutableListOf()
        for (uuid in iterator()) {
            result.addLast(uuid.uuid.toString())
        }
        return result
    }

    fun android.bluetooth.BluetoothClass.toSource(): BluetoothDeviceClass {
        val majorDeviceClass: BluetoothMajorDeviceClass = when (majorDeviceClass) {
            Major.AUDIO_VIDEO -> BluetoothMajorDeviceClass.AUDIO_VIDEO
            Major.COMPUTER -> BluetoothMajorDeviceClass.COMPUTER
            Major.IMAGING -> BluetoothMajorDeviceClass.IMAGING
            Major.MISC -> BluetoothMajorDeviceClass.MISC
            Major.NETWORKING -> BluetoothMajorDeviceClass.NETWORKING
            Major.PERIPHERAL -> BluetoothMajorDeviceClass.PERIPHERAL
            Major.PHONE -> BluetoothMajorDeviceClass.PHONE
            Major.TOY -> BluetoothMajorDeviceClass.TOY
            Major.UNCATEGORIZED -> BluetoothMajorDeviceClass.UNCATEGORIZED
            Major.WEARABLE -> BluetoothMajorDeviceClass.WEARABLE
            else -> BluetoothMajorDeviceClass.UNCATEGORIZED
        }

        val minorDeviceClass: BluetoothMinorDeviceClass = when (deviceClass) {
            Device.AUDIO_VIDEO_CAMCORDER -> BluetoothMinorDeviceClass.AUDIO_VIDEO_CAMCORDER
            Device.AUDIO_VIDEO_CAR_AUDIO -> BluetoothMinorDeviceClass.AUDIO_VIDEO_CAR_AUDIO
            Device.AUDIO_VIDEO_HANDSFREE -> BluetoothMinorDeviceClass.AUDIO_VIDEO_HANDSFREE
            Device.AUDIO_VIDEO_HEADPHONES -> BluetoothMinorDeviceClass.AUDIO_VIDEO_HEADPHONES
            Device.AUDIO_VIDEO_HIFI_AUDIO -> BluetoothMinorDeviceClass.AUDIO_VIDEO_HIFI_AUDIO
            Device.AUDIO_VIDEO_LOUDSPEAKER -> BluetoothMinorDeviceClass.AUDIO_VIDEO_LOUDSPEAKER
            Device.AUDIO_VIDEO_MICROPHONE -> BluetoothMinorDeviceClass.AUDIO_VIDEO_MICROPHONE
            Device.AUDIO_VIDEO_PORTABLE_AUDIO -> BluetoothMinorDeviceClass.AUDIO_VIDEO_PORTABLE_AUDIO
            Device.AUDIO_VIDEO_SET_TOP_BOX -> BluetoothMinorDeviceClass.AUDIO_VIDEO_SET_TOP_BOX
            Device.AUDIO_VIDEO_UNCATEGORIZED -> BluetoothMinorDeviceClass.AUDIO_VIDEO_UNCATEGORIZED
            Device.AUDIO_VIDEO_VCR -> BluetoothMinorDeviceClass.AUDIO_VIDEO_VCR
            Device.AUDIO_VIDEO_VIDEO_CAMERA -> BluetoothMinorDeviceClass.AUDIO_VIDEO_VIDEO_CAMERA
            Device.AUDIO_VIDEO_VIDEO_CONFERENCING -> BluetoothMinorDeviceClass.AUDIO_VIDEO_VIDEO_CONFERENCING
            Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER -> BluetoothMinorDeviceClass.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER
            Device.AUDIO_VIDEO_VIDEO_GAMING_TOY -> BluetoothMinorDeviceClass.AUDIO_VIDEO_VIDEO_GAMING_TOY
            Device.AUDIO_VIDEO_VIDEO_MONITOR -> BluetoothMinorDeviceClass.AUDIO_VIDEO_VIDEO_MONITOR
            Device.AUDIO_VIDEO_WEARABLE_HEADSET -> BluetoothMinorDeviceClass.AUDIO_VIDEO_WEARABLE_HEADSET
            Device.COMPUTER_DESKTOP -> BluetoothMinorDeviceClass.COMPUTER_DESKTOP
            Device.COMPUTER_HANDHELD_PC_PDA -> BluetoothMinorDeviceClass.COMPUTER_HANDHELD_PC_PDA
            Device.COMPUTER_LAPTOP -> BluetoothMinorDeviceClass.COMPUTER_LAPTOP
            Device.COMPUTER_PALM_SIZE_PC_PDA -> BluetoothMinorDeviceClass.COMPUTER_PALM_SIZE_PC_PDA
            Device.COMPUTER_SERVER -> BluetoothMinorDeviceClass.COMPUTER_SERVER
            Device.COMPUTER_UNCATEGORIZED -> BluetoothMinorDeviceClass.COMPUTER_UNCATEGORIZED
            Device.COMPUTER_WEARABLE -> BluetoothMinorDeviceClass.COMPUTER_WEARABLE
            Device.HEALTH_BLOOD_PRESSURE -> BluetoothMinorDeviceClass.HEALTH_BLOOD_PRESSURE
            Device.HEALTH_DATA_DISPLAY -> BluetoothMinorDeviceClass.HEALTH_DATA_DISPLAY
            Device.HEALTH_GLUCOSE -> BluetoothMinorDeviceClass.HEALTH_GLUCOSE
            Device.HEALTH_PULSE_OXIMETER -> BluetoothMinorDeviceClass.HEALTH_PULSE_OXIMETER
            Device.HEALTH_PULSE_RATE -> BluetoothMinorDeviceClass.HEALTH_PULSE_RATE
            Device.HEALTH_THERMOMETER -> BluetoothMinorDeviceClass.HEALTH_THERMOMETER
            Device.HEALTH_UNCATEGORIZED -> BluetoothMinorDeviceClass.HEALTH_UNCATEGORIZED
            Device.HEALTH_WEIGHING -> BluetoothMinorDeviceClass.HEALTH_WEIGHING
            Device.PERIPHERAL_KEYBOARD -> BluetoothMinorDeviceClass.PERIPHERAL_KEYBOARD
            Device.PERIPHERAL_KEYBOARD_POINTING -> BluetoothMinorDeviceClass.PERIPHERAL_KEYBOARD_POINTING
            Device.PERIPHERAL_NON_KEYBOARD_NON_POINTING -> BluetoothMinorDeviceClass.PERIPHERAL_NON_KEYBOARD_NON_POINTING
            Device.PERIPHERAL_POINTING -> BluetoothMinorDeviceClass.PERIPHERAL_POINTING
            Device.PHONE_CELLULAR -> BluetoothMinorDeviceClass.PHONE_CELLULAR
            Device.PHONE_CORDLESS -> BluetoothMinorDeviceClass.PHONE_CORDLESS
            Device.PHONE_ISDN -> BluetoothMinorDeviceClass.PHONE_ISDN
            Device.PHONE_MODEM_OR_GATEWAY -> BluetoothMinorDeviceClass.PHONE_MODEM_OR_GATEWAY
            Device.PHONE_SMART -> BluetoothMinorDeviceClass.PHONE_SMART
            Device.PHONE_UNCATEGORIZED -> BluetoothMinorDeviceClass.PHONE_UNCATEGORIZED
            Device.TOY_CONTROLLER -> BluetoothMinorDeviceClass.TOY_CONTROLLER
            Device.TOY_DOLL_ACTION_FIGURE -> BluetoothMinorDeviceClass.TOY_DOLL_ACTION_FIGURE
            Device.TOY_GAME -> BluetoothMinorDeviceClass.TOY_GAME
            Device.TOY_ROBOT -> BluetoothMinorDeviceClass.TOY_ROBOT
            Device.TOY_UNCATEGORIZED -> BluetoothMinorDeviceClass.TOY_UNCATEGORIZED
            Device.TOY_VEHICLE -> BluetoothMinorDeviceClass.TOY_VEHICLE
            Device.WEARABLE_GLASSES -> BluetoothMinorDeviceClass.WEARABLE_GLASSES
            Device.WEARABLE_HELMET -> BluetoothMinorDeviceClass.WEARABLE_HELMET
            Device.WEARABLE_JACKET -> BluetoothMinorDeviceClass.WEARABLE_JACKET
            Device.WEARABLE_PAGER -> BluetoothMinorDeviceClass.WEARABLE_PAGER
            Device.WEARABLE_UNCATEGORIZED -> BluetoothMinorDeviceClass.WEARABLE_UNCATEGORIZED
            Device.WEARABLE_WRIST_WATCH -> BluetoothMinorDeviceClass.WEARABLE_WRIST_WATCH
            else -> BluetoothMinorDeviceClass.UNCATEGORIZED
        }

        return BluetoothDeviceClass(
            majorDeviceClass = majorDeviceClass,
            minorDeviceClass = minorDeviceClass,
        )
    }

    fun Int.connectionStateToSource(): BluetoothDeviceConnectionState {
        return when (this) {
            BluetoothAdapter.STATE_DISCONNECTED -> BluetoothDeviceConnectionState.STATE_DISCONNECTED
            BluetoothAdapter.STATE_CONNECTING -> BluetoothDeviceConnectionState.STATE_CONNECTING
            BluetoothAdapter.STATE_CONNECTED -> BluetoothDeviceConnectionState.STATE_CONNECTED
            BluetoothAdapter.STATE_DISCONNECTING -> BluetoothDeviceConnectionState.STATE_DISCONNECTING
            else -> BluetoothDeviceConnectionState.STATE_DISCONNECTED
        }
    }

    fun Int.addressTypeToSource(): BluetoothAddressType {
        return when (this) {
            BluetoothDevice.ADDRESS_TYPE_PUBLIC -> BluetoothAddressType.ADDRESS_TYPE_PUBLIC
            BluetoothDevice.ADDRESS_TYPE_RANDOM -> BluetoothAddressType.ADDRESS_TYPE_RANDOM
            BluetoothDevice.ADDRESS_TYPE_ANONYMOUS -> BluetoothAddressType.ADDRESS_TYPE_ANONYMOUS
            BluetoothDevice.ADDRESS_TYPE_UNKNOWN -> BluetoothAddressType.ADDRESS_TYPE_UNKNOWN
            else -> BluetoothAddressType.ADDRESS_TYPE_UNKNOWN
        }
    }
}