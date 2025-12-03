package com.jayden.BluetoothManager.adapter

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jayden.BluetoothManager.MainApplication
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.adapter.exception.AdapterNotOnException
import com.jayden.BluetoothManager.adapter.exception.PermissionException
import com.jayden.BluetoothManager.databinding.FragmentBluetoothAdapterBinding
import com.jayden.BluetoothManager.device.BoundDevicesFragment
import com.jayden.BluetoothManager.device.DeviceCompatUi
import com.jayden.BluetoothManager.permission.PermissionHelper
import com.jayden.BluetoothManager.scanner.BluetoothScannerFragment
import kotlinx.coroutines.launch

class LocalAdapterFragment : Fragment(R.layout.fragment_bluetooth_adapter) {
    private var _binding: FragmentBluetoothAdapterBinding? = null
    private val binding get() = _binding!!

    private val fragments = listOf(ADAPTER_STATE, PAIRED_DEVICES, SCAN_FRAGMENT)

    private val viewModel by viewModels<LocalAdapterViewModel>(
        ownerProducer = { this },
        factoryProducer = { LocalAdapterViewModelFactory((requireActivity().application as MainApplication).applicationGraph) }
    )

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            Log.d(TAG, "tabSelectedListener::onTabSelected(${tab?.text})")
            childFragmentManager.commitNow {
                val target = requireFragment(tab!!.tag as String)
                if (target.isHidden) {
                    show(target)
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            Log.d(TAG, "tabSelectedListener::onTabUnselected(${tab?.text})")
            childFragmentManager.commitNow {
                for (fragment in fragments) {
                    val frag = requireFragment(fragment)
                    if (!frag.isHidden) hide(frag)
                }
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            Log.d(TAG, "tabSelectedListener::onTabReselected(${tab?.text})")
            for (fragment in fragments) {
                val frag = getFragment(fragment)
                if (frag != null)
                Log.v(TAG, "${frag.tag}: hidden = ${frag.isHidden}, " +
                        "added = ${frag.isAdded}, " +
                        "detached = ${frag.isDetached}, " +
                        "visible = ${frag.isVisible}, " +
                        "viewVisibility = ${when (frag.view?.visibility) {
                                View.VISIBLE -> "VISIBLE"
                                View.GONE -> "GONE"
                                View.INVISIBLE -> "INVISIBLE"
                                null -> "null"
                                else -> "<unknown state>"
                            }
                        } " +
                        "hash = ${frag.hashCode()}, " +
                        "viewHash = ${frag.view.hashCode()}"
                )
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothAdapterBinding.inflate(inflater, container, false)
        Log.v(TAG, "onCreateView(\n    inflater = $inflater, \n    container = $container, \n    savedInstanceState = $savedInstanceState\n): View = ${binding.root}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "onViewCreated(\n    view = $view, \n    savedInstanceState = $savedInstanceState\n)")

        binding.tabLayout.addOnTabSelectedListener(tabSelectedListener)

        if (savedInstanceState == null) {
            Log.d(TAG, "onViewCreated\$savedInstanceState = null")
            childFragmentManager.beginTransaction().apply {
                for (fragment in fragments) {
                    val newFrag = newFragment(fragment)
                    add(R.id.fragment_container, newFrag, fragment)
                    Log.v(TAG, "hash of $fragment: ${newFrag.hashCode()}")
                    Log.v(TAG, "has of view: ${newFrag.view.hashCode()}")
                    hide(newFrag)
                }
                commitNow()
            }

            Log.v(TAG, "detached all but the default selected fragment")
        } else {
            Log.d(TAG, "savedInstanceState != null")
        }

        binding.tabLayout.apply {
            addTab(newTab().setText("Adapter").setTag(ADAPTER_STATE))
            addTab(newTab().setText("Paired Devices").setTag(PAIRED_DEVICES))
            addTab(newTab().setText("Scan").setTag(SCAN_FRAGMENT))
            Log.v(TAG, "addTab(newTab().setText(\"...\").setTag(CONST)) * 3")
        }

        try {
            viewModel.start()
        } catch (e: AdapterNotOnException) {
            Log.w(TAG, "Adapter is not on", e)
            val launcher = requireActivity().registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.start()
                }
            }

            if (PermissionHelper.isGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        } catch (e: SecurityException) {
            viewLifecycleOwner.lifecycleScope.launch {
                val permissionResults = PermissionHelper.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ))

                // loop through granted perms
                for (result in permissionResults) {
                    if (result.value) {
                        Log.i(TAG, "permission: ${result.key}, has been granted.")
                    } else {
                        Log.i(TAG, "permission: ${result.key}, has not been granted.")
                        if (result.key == Manifest.permission.BLUETOOTH_CONNECT) {
                            Log.w(TAG, "permission needed has not been granted")
                            Toast.makeText(context, "Permission BLUETOOTH_CONNECT has not been granted", Toast.LENGTH_LONG).show()
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
        binding.tabLayout.removeOnTabSelectedListener(tabSelectedListener)
        _binding = null
        super.onDestroyView()
    }

    private fun getFragment(tag: String): Fragment? {
        return childFragmentManager.findFragmentByTag(tag)
    }

    private fun requireFragment(tag: String): Fragment {
        return getFragment(tag)!!
    }

    private fun newFragment(tag: String): Fragment {
        return when (tag) {
            ADAPTER_STATE -> AdapterStateFragment()
            PAIRED_DEVICES -> BoundDevicesFragment()
            SCAN_FRAGMENT -> BluetoothScannerFragment()
            else -> throw IllegalArgumentException("increase the size of the fragment list?")
        }
    }

    companion object {
        private const val TAG = "LocalAdapterFragment"
        private const val ADAPTER_STATE = "adapter-state"
        private const val PAIRED_DEVICES = "paired-devices"
        private const val SCAN_FRAGMENT = "scan-fragment"
    }
}