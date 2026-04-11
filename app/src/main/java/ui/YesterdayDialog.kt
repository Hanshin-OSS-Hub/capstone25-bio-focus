package com.cookandroid.capstone2.ui

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookandroid.capstone2.databinding.ActivityMainBinding
import com.cookandroid.capstone2.databinding.DialogYesterdayBinding
import com.cookandroid.capstone2.viewmodel.PlannerViewModel
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

        viewModel.getPlansByDate(yesterday).observe(activity) { plans ->
            adapter.submitList(plans)
            if (plans.isEmpty()) {
                binding.tvYesterdayEmpty.visibility = android.view.View.VISIBLE
            } else {
                binding.tvYesterdayEmpty.visibility = android.view.View.GONE
            }
        }

        binding.btnYesterdayClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}