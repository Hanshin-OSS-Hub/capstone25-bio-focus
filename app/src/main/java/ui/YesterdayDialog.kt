package com.cookandroid.capstone2.ui

import android.app.Dialog
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import data.StudyDatabase
import com.cookandroid.capstone2.databinding.DialogYesterdayBinding
import health.HealthConnectManager
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class YesterdayDialog(
    private val activity: AppCompatActivity,
    private val viewModel: PlannerViewModel
) {
    fun show() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogYesterdayBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val yesterday = LocalDate.now().minusDays(1).toString()
        binding.tvYesterdayDate.text = "${yesterday} 학습 결과"

        val adapter = YesterdayAdapter()
        binding.rvYesterday.layoutManager = LinearLayoutManager(activity)
        binding.rvYesterday.adapter = adapter

        activity.lifecycleScope.launch {
            syncYesterdayHrv(yesterday)
        }

        viewModel.getPlansByDate(yesterday).observe(activity) { plans ->
            adapter.submitList(plans)
            binding.tvYesterdayEmpty.visibility =
                if (plans.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.btnYesterdayClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private suspend fun syncYesterdayHrv(date: String) {
        val manager = HealthConnectManager(activity)

        if (!manager.isAvailable()) {
            Log.d("HealthConnect", "Health Connect 사용 불가")
            return
        }

        if (!manager.hasAllPermissions()) {
            Log.d("HealthConnect", "HRV 읽기 권한 없음")
            return
        }

        withContext(Dispatchers.IO) {
            val dao = StudyDatabase.getDatabase(activity.applicationContext).studyPlanDao()
            val plans = dao.getPlansByDateOnce(date)

            plans.forEach { plan ->
                val startMillis = plan.sessionStartMillis
                val endMillis = plan.sessionEndMillis

                if (startMillis == null || endMillis == null) return@forEach

                val averageHrv = manager.readAverageHrvRmssd(startMillis, endMillis)

                dao.updatePlan(
                    plan.copy(
                        hrvRmssd = averageHrv,
                        hrvSyncedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}