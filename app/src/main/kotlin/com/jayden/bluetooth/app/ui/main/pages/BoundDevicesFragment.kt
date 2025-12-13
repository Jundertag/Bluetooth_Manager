package com.jayden.bluetooth.app.ui.main.pages

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jayden.bluetooth.MainApplication
import com.jayden.bluetooth.app.adapter.LocalDeviceAdapter
import com.jayden.bluetooth.app.viewmodel.main.pages.LocalAdapterViewModel
import com.jayden.bluetooth.app.viewmodel.main.pages.LocalAdapterViewModelFactory
import com.jayden.bluetooth.data.device.DeviceEvent
import com.jayden.bluetooth.databinding.FragmentPairedDevicesBinding
import com.jayden.bluetooth.model.DeviceCompatUi
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.collections.iterator

class BoundDevicesFragment : Fragment() {
    private var _binding: FragmentPairedDevicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LocalDeviceAdapter

    private val viewModel by viewModels<LocalAdapterViewModel>(
        ownerProducer = { requireParentFragment() },
        factoryProducer = {
            LocalAdapterViewModelFactory((requireActivity().application as MainApplication).applicationGraph)
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPairedDevicesBinding.inflate(inflater, container, false)
        Log.v(TAG, "onCreateView(\n    inflater = $inflater, \n    container = $container, \n    savedInstanceState = $savedInstanceState\n): View = ${binding.root}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "onViewCreated(\n    view = $view, \n    savedInstanceState = $savedInstanceState\n)")
        adapter = LocalDeviceAdapter()

        binding.pairedDevicesView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BoundDevicesFragment.adapter
        }.also {
            Log.v(TAG, "set layoutManager to reference a new LinearLayoutManager\nset adapter to reference LocalDeviceAdapter for device display list")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.boundDevices.collect { devices ->
                    adapter.submitList(devices.mapNotNull { it.ui.flowWithLifecycle(viewLifecycleOwner.lifecycle).firstOrNull() })
                }
            }
        }
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
        Log.i(TAG, "onDestroyView()")
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "BoundDevicesFragment"
    }
}