package com.cookandroid.capstone2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookandroid.capstone2.databinding.ActivityMainBinding
import com.cookandroid.capstone2.ui.AddPlanActivity
import com.cookandroid.capstone2.ui.PlanAdapter
import com.cookandroid.capstone2.ui.StudyActivity
import com.cookandroid.capstone2.ui.YesterdayDialog
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
import com.cookandroid.capstone2.ui.WeekDataActivity
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PlannerViewModel
    private lateinit var adapter: PlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlannerViewModel::class.java]

        val today = LocalDate.now().toString()
        binding.tvDate.text = today

        adapter = PlanAdapter(
            onComplete = { viewModel.toggleComplete(it) },
            onDelete = { viewModel.deletePlan(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.getPlansByDate(today).observe(this) { plans ->
            adapter.submitList(plans)
            binding.tvEmpty.visibility =
                if (plans.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddPlanActivity::class.java))
        }

        binding.btnStudyStart.setOnClickListener {
            startActivity(Intent(this, StudyActivity::class.java))
        }
        binding.btnWeekData.setOnClickListener {
            startActivity(Intent(this, WeekDataActivity::class.java))
        }
        binding.btnYesterday.setOnClickListener {
            YesterdayDialog(this, viewModel).show()
        }
    }
}