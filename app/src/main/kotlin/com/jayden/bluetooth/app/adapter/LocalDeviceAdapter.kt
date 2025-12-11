package com.jayden.bluetooth.app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jayden.bluetooth.databinding.ItemDeviceViewBinding
import com.jayden.bluetooth.model.DeviceCompatUi

class LocalDeviceAdapter : ListAdapter<DeviceCompatUi, LocalDeviceAdapter.ViewHolder>(DiffCallback) {


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
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemDeviceViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeviceCompatUi) {
            binding.deviceAddress.text = item.address
            binding.deviceTitle.text = item.name
            binding.deviceRssi.text = item.rssi
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<DeviceCompatUi>() {
        override fun areItemsTheSame(
            oldItem: DeviceCompatUi,
            newItem: DeviceCompatUi
        ): Boolean {
            Log.d(TAG, "DiffCallback::areItemsTheSame($oldItem, $newItem): ${oldItem.address == newItem.address}")
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: DeviceCompatUi,
            newItem: DeviceCompatUi
        ): Boolean {
            return oldItem == newItem
        }

    }

    companion object {
        private const val TAG = "LocalDeviceAdapter"
    }
}