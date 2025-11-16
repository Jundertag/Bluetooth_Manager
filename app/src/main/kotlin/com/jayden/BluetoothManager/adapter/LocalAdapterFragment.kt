package com.jayden.BluetoothManager.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jayden.BluetoothManager.MainApplication
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.databinding.FragmentBluetoothAdapterBinding
import kotlinx.coroutines.launch

class LocalAdapterFragment : Fragment() {
    private var _binding: FragmentBluetoothAdapterBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LocalAdapterViewModel> {
        LocalAdapterViewModelFactory((requireActivity().application as MainApplication).applicationGraph)
    }

    private lateinit var adapter: LocalDeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothAdapterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = LocalDeviceAdapter()

        binding.pairedDevicesView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.boundDevices.collect { devices ->

            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}