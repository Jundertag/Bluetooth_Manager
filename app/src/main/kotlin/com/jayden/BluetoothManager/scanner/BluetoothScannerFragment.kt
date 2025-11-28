package com.jayden.BluetoothManager.scanner

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jayden.BluetoothManager.adapter.LocalAdapterViewModel

class BluetoothScannerFragment : Fragment() {
    private val viewModel by viewModels<LocalAdapterViewModel>(ownerProducer = { requireParentFragment() })



    companion object {
        private const val TAG = "BluetoothScannerFragment"
    }
}