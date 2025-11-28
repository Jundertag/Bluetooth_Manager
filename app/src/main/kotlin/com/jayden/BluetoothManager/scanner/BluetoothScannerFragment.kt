package com.jayden.BluetoothManager.scanner

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jayden.BluetoothManager.MainApplication

class BluetoothScannerFragment : Fragment() {
    private val viewModel by viewModels<BluetoothScannerViewModel> {
        BluetoothScannerViewModelFactory((requireActivity().application as MainApplication).applicationGraph)
    }

    companion object {
        private const val TAG = "BluetoothScannerFragment"
    }
}