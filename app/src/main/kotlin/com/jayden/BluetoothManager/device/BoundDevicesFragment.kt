package com.jayden.BluetoothManager.device

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jayden.BluetoothManager.MainApplication
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.adapter.LocalDeviceAdapter
import com.jayden.BluetoothManager.adapter.LocalAdapterViewModel
import com.jayden.BluetoothManager.databinding.FragmentPairedDevicesBinding
import kotlinx.coroutines.launch

class BoundDevicesFragment : Fragment() {
    private var _binding: FragmentPairedDevicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LocalDeviceAdapter

    private val viewModel by viewModels<LocalAdapterViewModel>(ownerProducer = { requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPairedDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = LocalDeviceAdapter()

        binding.pairedDevicesView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BoundDevicesFragment.adapter
        }

        viewModel.start()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.boundDevices.collect { devices ->
                val deviceCompatUi = mutableListOf<DeviceCompatUi>()
                devices.forEach { device ->
                    val compatUi = DeviceCompatUi(
                        name = device.name,
                        address = device.address
                    )
                    deviceCompatUi.add(compatUi)
                }
                adapter.submitList(deviceCompatUi)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "BoundDevicesFragment"
    }
}