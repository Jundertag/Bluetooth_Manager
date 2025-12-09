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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jayden.bluetooth.adapter.LocalDeviceAdapter
import com.jayden.bluetooth.app.viewmodel.main.pages.LocalAdapterViewModel
import com.jayden.bluetooth.databinding.FragmentPairedDevicesBinding
import com.jayden.bluetooth.device.DeviceCompatUi
import com.jayden.bluetooth.utils.PermissionHelper
import kotlinx.coroutines.launch
import kotlin.collections.iterator

class BoundDevicesFragment : Fragment() {
    private var _binding: FragmentPairedDevicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LocalDeviceAdapter

    private val viewModel by viewModels<LocalAdapterViewModel>(ownerProducer = { requireParentFragment() })

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
            Log.v(TAG, "pairedDevicesView.layoutManager = ${it.layoutManager}\npairedDevicesView.adapter = ${it.adapter}")
        }

        if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.boundDevices.collect { devices ->
                        val deviceCompatUi = mutableListOf<DeviceCompatUi>()
                        devices.forEach { device ->
                            val compatUi = DeviceCompatUi(
                                name = device.name,
                                address = device.address
                            )
                            deviceCompatUi.add(compatUi)
                        }
                        Log.v(TAG, "current devices: \n$deviceCompatUi")
                        adapter.submitList(deviceCompatUi) {
                            Log.v(TAG, "submitted list successfully")
                        }
                    }
                }
            }
        } else {
            // TODO: request permissions
            viewLifecycleOwner.lifecycleScope.launch {
                val permissionResults = PermissionHelper.requestPermissions(requireActivity(), arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                )

                for (result in permissionResults) {
                    if (result.value) {
                        Log.i(TAG, "permission: ${result.key}, has been granted.")
                        if (result.key == Manifest.permission.BLUETOOTH_CONNECT) viewModel.permissionGranted()
                    } else {
                        Log.i(TAG, "permission: ${result.key}, has not been granted.")
                        if (result.key == Manifest.permission.BLUETOOTH_CONNECT) {
                            Log.w(TAG, "required permission: ${result.key}")
                            Toast.makeText(context, "permission BLUETOOTH_CONNECT is required for normal function.", Toast.LENGTH_LONG).show()
                        }
                    }
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