package com.jayden.bluetooth.data.model

data class BluetoothLocalModel(
    val name: String,
    val leMaxAdvertisingDataLength: Int,
    val discovering: Boolean,
    val state: BluetoothLocalState,
    val le2MPhySupport: Boolean,
    val leAudioBroadcastAssistSupport: BluetoothCodes,
    val leAudioBroadcastSourceSupport: BluetoothCodes,
    val leAudioSupport: BluetoothCodes,
    val leCodedPhySupport: Boolean,
    val leExtendedAdvertisingSupport: Boolean,
    val lePeriodicAdvertisingSupport: Boolean,
    val multipleAdvertisementSupport: Boolean,
    val offloadedFilteringSupport: Boolean,
    val offloadedScanBatchingSupport: Boolean,
)