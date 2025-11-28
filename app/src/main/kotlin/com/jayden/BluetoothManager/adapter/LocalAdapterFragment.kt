package com.jayden.BluetoothManager.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jayden.BluetoothManager.MainApplication
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.databinding.FragmentBluetoothAdapterBinding
import com.jayden.BluetoothManager.device.BoundDevicesFragment
import com.jayden.BluetoothManager.device.DeviceCompatUi
import com.jayden.BluetoothManager.scanner.BluetoothScannerFragment
import kotlinx.coroutines.launch

class LocalAdapterFragment : Fragment(R.layout.fragment_bluetooth_adapter) {
    private var _binding: FragmentBluetoothAdapterBinding? = null
    private val binding get() = _binding!!

    private val fragments = mapOf(ADAPTER_STATE to AdapterStateFragment(), PAIRED_DEVICES to BoundDevicesFragment(), SCAN_FRAGMENT to BluetoothScannerFragment())

    private val viewModel by viewModels<LocalAdapterViewModel> {
        LocalAdapterViewModelFactory((requireActivity().application as MainApplication).applicationGraph)
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            Log.d(TAG, "TabLayout.OnTabSelectedListener::onTabSelected(${tab?.text})")
            childFragmentManager.commit {
                attach(fragments[tab?.tag]!!)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            Log.d(TAG, "TabLayout.OnTabSelectedListener::onTabUnelected(${tab?.text})")
            childFragmentManager.commit {
                detach(fragments[tab?.tag]!!)
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            Log.d(TAG, "TabLayout.OnTabSelectedListener::onTabReselected(${tab?.text})")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothAdapterBinding.inflate(inflater, container, false)
        Log.v(TAG, "onCreateView(\ninflater: LayoutInflater = $inflater, \ncontainer: ViewGroup? = $container, \nsavedInstanceState: Bundle? = $savedInstanceState\n): View = ${binding.root}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "onViewCreated(\nview: View = $view, \nsavedInstanceState: Bundle? = $savedInstanceState\n)")
        binding.tabLayout.apply {
            addTab(newTab().setText("Adapter").setTag(ADAPTER_STATE))
            addTab(newTab().setText("Paired Devices").setTag(PAIRED_DEVICES))
            addTab(newTab().setText("Scan").setTag(SCAN_FRAGMENT))
            Log.v(TAG, "added tabs")
        }.also {
            childFragmentManager.beginTransaction().apply {
                for (fragment in fragments) {
                    add(R.id.fragment_container, fragment.value, fragment.key)
                    detach(fragment.value)
                }
                attach(fragments[ADAPTER_STATE]!!)
                Log.i(TAG, "adding and detaching all but fragments[ADAPTER_STATE] fragment")
                commitNow()
            }
            Log.v(TAG, "committed")
        }

        binding.tabLayout.addOnTabSelectedListener(tabSelectedListener)

    }

    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView()")
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "LocalAdapterFragment"
        private const val ADAPTER_STATE = "adapter-state"
        private const val PAIRED_DEVICES = "paired-devices"
        private const val SCAN_FRAGMENT = "scan-fragment"
    }
}