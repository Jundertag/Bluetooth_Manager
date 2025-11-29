package com.jayden.BluetoothManager.scanner

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jayden.BluetoothManager.adapter.LocalAdapterViewModel
import com.jayden.BluetoothManager.databinding.FragmentBluetoothScannerBinding

class BluetoothScannerFragment : Fragment() {
    private var _binding: FragmentBluetoothScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LocalAdapterViewModel>(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothScannerBinding.inflate(inflater, container, false)
        Log.v(TAG, "onCreateView(\n    inflater = $inflater, \n    container = $container, \n    savedInstanceState = $savedInstanceState\n): View = ${binding.root}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "onViewCreated(\n    view = $view, \n    savedInstanceState = $savedInstanceState\n)")
    }

    companion object {
        private const val TAG = "BluetoothScannerFragment"
    }
}