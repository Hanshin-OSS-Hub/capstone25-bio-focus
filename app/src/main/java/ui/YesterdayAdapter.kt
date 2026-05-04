package com.cookandroid.capstone2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.capstone2.R
import data.StudyPlan
import com.cookandroid.capstone2.databinding.ItemYesterdayBinding
import kotlin.math.roundToInt

class YesterdayAdapter :
    ListAdapter<StudyPlan, YesterdayAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemYesterdayBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: StudyPlan) {
            binding.tvYesterdaySubject.text = plan.subject
            binding.tvYesterdayTitle.text = plan.title

            // 시작시간/종료시간을 사용하지 않으므로 기존 시간 표시 영역 숨김
            binding.tvYesterdayTime.visibility = View.GONE

            val statusText = if (plan.isCompleted) "완료" else "미완료"
            val hrvText = plan.hrvRmssd?.let {
                " / HRV ${it.roundToInt()} ms"
            } ?: " / HRV 없음"

            binding.tvYesterdayStatus.text = statusText + hrvText

            if (plan.isCompleted) {
                binding.tvYesterdayStatus.setTextColor(
                    binding.root.context.getColor(R.color.btn_done)
                )
            } else {
                binding.tvYesterdayStatus.setTextColor(
                    binding.root.context.getColor(R.color.subj_history)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemYesterdayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
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