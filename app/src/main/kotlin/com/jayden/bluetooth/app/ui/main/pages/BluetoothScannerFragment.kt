package com.jayden.bluetooth.app.ui.main.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jayden.bluetooth.MainApplication
import com.jayden.bluetooth.app.viewmodel.main.pages.LocalAdapterViewModel
import com.jayden.bluetooth.app.viewmodel.main.pages.LocalAdapterViewModelFactory
import com.jayden.bluetooth.databinding.FragmentBluetoothScannerBinding

class BluetoothScannerFragment : Fragment() {
    private var _binding: FragmentBluetoothScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LocalAdapterViewModel>(
        ownerProducer = { requireParentFragment() },
        factoryProducer = {
            LocalAdapterViewModelFactory((requireActivity().application as MainApplication).applicationGraph)
        }
    )

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

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.v(TAG, "onResume()")
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.v(TAG, "onHiddenChanged(hidden = $hidden)")
    }

    override fun onPause() {
        Log.v(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.v(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.w(TAG, "onDestroyView()")
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "BluetoothScannerFragment"
    }
}