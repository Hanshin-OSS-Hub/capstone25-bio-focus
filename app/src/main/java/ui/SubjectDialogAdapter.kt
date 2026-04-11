package com.cookandroid.capstone2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.capstone2.data.StudyPlan
import com.cookandroid.capstone2.databinding.ItemSubjectBinding

class SubjectDialogAdapter(
    private val plans: List<StudyPlan>,
    private val onSelect: (StudyPlan) -> Unit
) : RecyclerView.Adapter<SubjectDialogAdapter.ViewHolder>() {

    private var selectedPos = 0

    inner class ViewHolder(private val binding: ItemSubjectBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: StudyPlan, position: Int) {
            binding.tvSubjectName.text = plan.subject
            binding.tvSubjectTitle.text = plan.title
            binding.rbSelect.isChecked = (position == selectedPos)

            binding.root.setOnClickListener {
                selectedPos = position
                notifyDataSetChanged()
                onSelect(plan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(plans[position], position)
    }

    override fun getItemCount() = plans.size
}