package com.cookandroid.capstone2.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import data.StudyDatabase
import data.StudyPlan
import com.cookandroid.capstone2.databinding.ActivityAddPlanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AddPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            savePlan()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun savePlan() {
        val subject = binding.etSubject.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val targetText = binding.etTargetMinutes.text.toString().trim()
        val today = LocalDate.now().toString()

        if (subject.isEmpty() || title.isEmpty() || targetText.isEmpty()) {
            Toast.makeText(this, "과목, 할 일, 목표시간을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val targetMinutes = targetText.toIntOrNull()

        if (targetMinutes == null || targetMinutes <= 0) {
            Toast.makeText(this, "목표시간은 1분 이상 숫자로 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val isSaved = withContext(Dispatchers.IO) {
                val dao = StudyDatabase.getDatabase(applicationContext).studyPlanDao()
                val count = dao.getPlanCountByDate(today)

                if (count >= 5) {
                    false
                } else {
                    val plan = StudyPlan(
                        subject = subject,
                        title = title,
                        date = today,
                        targetMinutes = targetMinutes
                    )

                    dao.insertPlan(plan)
                    true
                }
            }

            if (isSaved) {
                Toast.makeText(this@AddPlanActivity, "할 일이 저장되었습니다", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this@AddPlanActivity,
                    "오늘 할 일은 최대 5개까지 추가할 수 있습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}