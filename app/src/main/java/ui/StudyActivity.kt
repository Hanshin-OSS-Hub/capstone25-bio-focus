package com.cookandroid.capstone2.ui

import android.app.Dialog
import android.os.Bundle
import android.os.SystemClock
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cookandroid.capstone2.data.StudyPlan
import com.cookandroid.capstone2.databinding.ActivityStudyBinding
import com.cookandroid.capstone2.databinding.DialogSubjectSelectBinding
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
import java.time.LocalDate

class StudyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBinding
    private lateinit var viewModel: PlannerViewModel

    private var isRunning = false
    private var elapsedTime = 0L
    private var startTime = 0L
    private lateinit var timerRunnable: Runnable
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private var selectedPlan: StudyPlan? = null
    private var planList: List<StudyPlan> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlannerViewModel::class.java]

        val today = LocalDate.now().toString()

        viewModel.getPlansByDate(today).observe(this) { plans ->
            planList = plans
            if (selectedPlan == null && plans.isNotEmpty()) {
                selectedPlan = plans.first()
                updateSubjectUI()
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
            selectedPlan?.let {
                viewModel.toggleComplete(it)
            }
            stopTimer()
            finish()
        }

        binding.btnStop.setOnClickListener {
            stopTimer()
            finish()
        }

        startTimer()
    }

    private fun startTimer() {
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

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}