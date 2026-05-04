package com.cookandroid.capstone2.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import data.StudyPlan
import com.cookandroid.capstone2.databinding.ItemPlanBinding

class PlanAdapter(
    private val onComplete: (StudyPlan) -> Unit,
    private val onDelete: (StudyPlan) -> Unit
) : ListAdapter<StudyPlan, PlanAdapter.PlanViewHolder>(DiffCallback()) {

    inner class PlanViewHolder(private val binding: ItemPlanBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: StudyPlan) {
            binding.tvSubject.text = "[${plan.subject}]"
            binding.tvTitle.text = plan.title
            binding.tvTime.text = "목표 ${plan.targetMinutes}분"

            if (plan.isCompleted) {
                binding.tvTitle.paintFlags =
                    binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvTitle.paintFlags =
                    binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.cbComplete.isChecked = plan.isCompleted
            binding.cbComplete.setOnClickListener { onComplete(plan) }
            binding.btnDelete.setOnClickListener { onDelete(plan) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<StudyPlan>() {
        override fun areItemsTheSame(old: StudyPlan, new: StudyPlan) = old.id == new.id
        override fun areContentsTheSame(old: StudyPlan, new: StudyPlan) = old == new
    }
}