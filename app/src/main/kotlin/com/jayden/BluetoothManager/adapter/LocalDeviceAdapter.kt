package com.jayden.BluetoothManager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jayden.BluetoothManager.R
import com.jayden.BluetoothManager.databinding.ItemDeviceViewBinding
import com.jayden.BluetoothManager.device.DeviceCompatUi

class LocalDeviceAdapter : ListAdapter<DeviceCompatUi, LocalDeviceAdapter.ViewHolder>(DiffCallback) {

    private val items = mutableListOf<DeviceCompatUi>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addOrUpdateDevice(device: DeviceCompatUi) {
        val newList = currentList.toMutableList()

        val index = newList.indexOfFirst { it.address == device.address }
        if (index == -1) {
            newList.add(device)
        } else {
            newList[index] = device
        }

        submitList(newList)
    }

    inner class ViewHolder(
        private val binding: ItemDeviceViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeviceCompatUi) {
            binding.deviceAddress.text = item.address
            binding.deviceTitle.text = item.name
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<DeviceCompatUi>() {
        override fun areItemsTheSame(
            oldItem: DeviceCompatUi,
            newItem: DeviceCompatUi
        ): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: DeviceCompatUi,
            newItem: DeviceCompatUi
        ): Boolean {
            return oldItem == newItem
        }

    }
}