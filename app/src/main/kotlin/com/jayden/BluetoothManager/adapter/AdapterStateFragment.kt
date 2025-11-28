package com.jayden.BluetoothManager.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.databinding.FragmentAdapterStateBinding

class AdapterStateFragment : Fragment() {
    private var _binding: FragmentAdapterStateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdapterStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.localAdapterName.text = "Local Adapter Name"

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}