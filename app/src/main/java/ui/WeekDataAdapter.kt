package com.cookandroid.capstone2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.capstone2.databinding.ItemWeekDataBinding

class WeekDataAdapter :
    ListAdapter<WeekDataItem, WeekDataAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemWeekDataBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WeekDataItem) {
            binding.tvDate.text = item.date
            binding.tvCount.text = "${item.count}개"
            binding.tvPercent.text = "${item.percent}%"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWeekDataBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<WeekDataItem>() {
        override fun areItemsTheSame(a: WeekDataItem, b: WeekDataItem) = a.date == b.date
        override fun areContentsTheSame(a: WeekDataItem, b: WeekDataItem) = a == b
    }
}