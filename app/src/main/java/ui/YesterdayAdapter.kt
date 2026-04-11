package com.cookandroid.capstone2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.capstone2.data.StudyPlan
import com.cookandroid.capstone2.databinding.ItemYesterdayBinding

class YesterdayAdapter :
    ListAdapter<StudyPlan, YesterdayAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemYesterdayBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: StudyPlan) {
            binding.tvYesterdaySubject.text = plan.subject
            binding.tvYesterdayTitle.text = plan.title
            binding.tvYesterdayTime.text = "${plan.startTime} ~ ${plan.endTime}"
            if (plan.isCompleted) {
                binding.tvYesterdayStatus.text = "완료"
                binding.tvYesterdayStatus.setTextColor(
                    binding.root.context.getColor(com.cookandroid.capstone2.R.color.btn_done)
                )
            } else {
                binding.tvYesterdayStatus.text = "미완료"
                binding.tvYesterdayStatus.setTextColor(
                    binding.root.context.getColor(com.cookandroid.capstone2.R.color.subj_history)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemYesterdayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<StudyPlan>() {
        override fun areItemsTheSame(a: StudyPlan, b: StudyPlan) = a.id == b.id
        override fun areContentsTheSame(a: StudyPlan, b: StudyPlan) = a == b
    }
}