package com.cookandroid.capstone2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookandroid.capstone2.databinding.ActivityWeekDataBinding
import com.cookandroid.capstone2.viewmodel.PlannerViewModel

class WeekDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeekDataBinding
    private lateinit var viewModel: PlannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeekDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlannerViewModel::class.java]

        val adapter = WeekDataAdapter()
        binding.rvWeekData.layoutManager = LinearLayoutManager(this)
        binding.rvWeekData.adapter = adapter

        viewModel.allPlans.observe(this) { plans ->
            val today = java.time.LocalDate.now().toString()

            val weekData = plans
                .filter { it.date != today }
                .groupBy { it.date }
                .entries
                .sortedByDescending { it.key }
                .take(7)
                .map { (date, dayPlans) ->
                    val total = dayPlans.size
                    val completed = dayPlans.count { it.isCompleted }
                    val percent = if (total > 0) (completed * 100 / total) else 0
                    WeekDataItem(date, total, percent)
                }

            adapter.submitList(weekData)
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}