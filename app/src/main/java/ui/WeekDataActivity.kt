package com.cookandroid.capstone2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookandroid.capstone2.databinding.ActivityWeekDataBinding
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
import kotlin.math.roundToInt

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
            val weekData = plans
                .groupBy { it.date }
                .entries
                .sortedByDescending { it.key }
                .take(7)
                .map { (date, dayPlans) ->
                    val targetMinutes = dayPlans.sumOf { it.targetMinutes }

                    val studiedMinutes = dayPlans.sumOf { plan ->
                        val start = plan.sessionStartMillis
                        val end = plan.sessionEndMillis

                        if (start != null && end != null && end > start) {
                            ((end - start) / 1000 / 60).toInt()
                        } else {
                            0
                        }
                    }

                    val percent = if (targetMinutes > 0) {
                        ((studiedMinutes.toDouble() / targetMinutes.toDouble()) * 100).roundToInt()
                    } else {
                        0
                    }

                    WeekDataItem(
                        date = date,
                        studiedMinutes = studiedMinutes,
                        targetMinutes = targetMinutes,
                        percent = percent
                    )
                }

            adapter.submitList(weekData)
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}