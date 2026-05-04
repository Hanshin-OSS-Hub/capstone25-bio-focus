package com.cookandroid.capstone2.ui

import android.app.Dialog
import android.os.Bundle
import android.os.SystemClock
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import data.StudyDatabase
import data.StudyPlan
import com.cookandroid.capstone2.databinding.ActivityStudyBinding
import com.cookandroid.capstone2.databinding.DialogSubjectSelectBinding
import health.HealthConnectManager
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StudyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBinding
    private lateinit var viewModel: PlannerViewModel
    private lateinit var healthConnectManager: HealthConnectManager

    private var isRunning = false
    private var elapsedTime = 0L
    private var startTime = 0L
    private lateinit var timerRunnable: Runnable
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private var selectedPlan: StudyPlan? = null
    private var planList: List<StudyPlan> = emptyList()

    private var sessionStartedAtMillis: Long? = null

    private val requestHealthPermissions =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            if (granted.containsAll(healthConnectManager.permissions)) {
                Toast.makeText(this, "헬스 커넥트 권한이 연결되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "HRV 동기화를 위해 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlannerViewModel::class.java]
        healthConnectManager = HealthConnectManager(this)

        val today = LocalDate.now().toString()

        viewModel.getPlansByDate(today).observe(this) { plans ->
            // 완료하지 않은 과목만 타이머 선택 목록에 표시
            val incompletePlans = plans.filter { !it.isCompleted }

            planList = incompletePlans

            // 현재 선택된 과목이 없거나, 이미 완료된 과목이면
            // 완료하지 않은 첫 번째 과목으로 자동 변경
            if (selectedPlan == null || selectedPlan?.isCompleted == true) {
                selectedPlan = incompletePlans.firstOrNull()
                updateSubjectUI()
            }

            // 현재 선택된 과목이 DB에서 완료 상태로 바뀐 경우도 대비
            selectedPlan?.let { current ->
                val latest = plans.find { it.id == current.id }

                if (latest?.isCompleted == true) {
                    selectedPlan = incompletePlans.firstOrNull()
                    updateSubjectUI()
                }
            }
        }

        timerRunnable = object : Runnable {
            override fun run() {
                val millis = elapsedTime + (SystemClock.elapsedRealtime() - startTime)
                val seconds = (millis / 1000).toInt()
                val h = seconds / 3600
                val m = (seconds % 3600) / 60
                val s = seconds % 60
                binding.tvTimer.text = String.format("%02d:%02d:%02d", h, m, s)
                handler.postDelayed(this, 500)
            }
        }

        binding.btnSubjectSelect.setOnClickListener {
            showSubjectDialog()
        }

        binding.btnPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
                binding.btnPause.text = "재개"
            } else {
                resumeTimer()
                binding.btnPause.text = "멈춤"
            }
        }

        binding.btnDone.setOnClickListener {
            lifecycleScope.launch {
                saveSessionAndFinish(markCompleted = true)
            }
        }

        binding.btnStop.setOnClickListener {
            lifecycleScope.launch {
                saveSessionAndFinish(markCompleted = false)
            }
        }

        startTimer()

        lifecycleScope.launch {
            ensureHealthPermissionIfNeeded()
        }
    }

    private fun startTimer() {
        if (sessionStartedAtMillis == null) {
            sessionStartedAtMillis = System.currentTimeMillis()
        }
        startTime = SystemClock.elapsedRealtime()
        isRunning = true
        handler.post(timerRunnable)
        binding.btnPause.text = "멈춤"
    }

    private fun pauseTimer() {
        elapsedTime += SystemClock.elapsedRealtime() - startTime
        isRunning = false
        handler.removeCallbacks(timerRunnable)
    }

    private fun resumeTimer() {
        startTime = SystemClock.elapsedRealtime()
        isRunning = true
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
        isRunning = false
    }

    private fun updateSubjectUI() {
        selectedPlan?.let {
            binding.tvCurrentSubject.text = it.subject
            binding.tvCurrentTitle.text = it.title
        }
    }

    private fun showSubjectDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dBinding = DialogSubjectSelectBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val adapter = SubjectDialogAdapter(planList) { plan ->
            selectedPlan = plan
            updateSubjectUI()
            dialog.dismiss()
        }

        dBinding.rvSubjects.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        dBinding.rvSubjects.adapter = adapter

        dBinding.btnDialogCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private suspend fun saveSessionAndFinish(markCompleted: Boolean) {
        stopTimer()

        val plan = selectedPlan ?: run {
            finish()
            return
        }

        val sessionEndMillis = System.currentTimeMillis()
        val sessionStartMillis = this.sessionStartedAtMillis ?: sessionEndMillis

        withContext(Dispatchers.IO) {
            val dao = StudyDatabase.getDatabase(applicationContext).studyPlanDao()
            val updatedPlan = plan.copy(
                isCompleted = if (markCompleted) true else plan.isCompleted,
                sessionStartMillis = sessionStartMillis,
                sessionEndMillis = sessionEndMillis
            )
            dao.updatePlan(updatedPlan)
        }

        finish()
    }

    private suspend fun ensureHealthPermissionIfNeeded() {
        if (!healthConnectManager.isAvailable()) return

        val hasPermission = healthConnectManager.hasAllPermissions()
        if (!hasPermission) {
            requestHealthPermissions.launch(healthConnectManager.permissions)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}