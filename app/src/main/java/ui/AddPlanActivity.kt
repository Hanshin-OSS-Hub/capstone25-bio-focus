package com.cookandroid.capstone2.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cookandroid.capstone2.databinding.ActivityAddPlanBinding
import com.cookandroid.capstone2.data.StudyPlan
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
import java.time.LocalDate

class AddPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlanBinding
    private lateinit var viewModel: PlannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlannerViewModel::class.java]

        binding.btnSave.setOnClickListener {
            val subject = binding.etSubject.text.toString().trim()
            val title = binding.etTitle.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
                .ifEmpty { LocalDate.now().toString() }
            val start = binding.etStartTime.text.toString().trim()
            val end = binding.etEndTime.text.toString().trim()

            if (subject.isEmpty() || title.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val plan = StudyPlan(
                subject = subject,
                title = title,
                date = date,
                startTime = start,
                endTime = end
            )
            viewModel.insertPlan(plan)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
}