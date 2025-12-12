// com/biofocus/concentration/ui/study/StudyEndActivity.kt
package com.biofocus.ui.study

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity          // ✅ 이걸로 변경
import com.biofocus.concentration.R
import com.biofocus.ui.main.MainActivity
import java.util.Locale

class StudyEndActivity : ComponentActivity() {

    private lateinit var completionProgressBar: ProgressBar
    private lateinit var completionTextView: TextView
    private lateinit var studyDurationTextView: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_end)

        completionProgressBar = findViewById(R.id.completion_progress_bar)
        completionTextView = findViewById(R.id.completion_text_view)
        studyDurationTextView = findViewById(R.id.study_duration_text_view)
        confirmButton = findViewById(R.id.confirm_button)

        val studyDurationMillis = intent.getLongExtra("STUDY_DURATION_MILLIS", 0L)
        val targetDurationMillis = intent.getLongExtra("TARGET_DURATION_MILLIS", 0L)

        // 공부 완료 시간 표시
        val seconds = (studyDurationMillis / 1000) % 60
        val minutes = (studyDurationMillis / (1000 * 60))
        studyDurationTextView.text = String.format(Locale.getDefault(), "%02d:%02d 완료.", minutes, seconds)

        // 목표 대비 달성률 계산 및 표시
        if (targetDurationMillis > 0) {
            val progress = ((studyDurationMillis.toFloat() / targetDurationMillis) * 100).toInt().coerceAtMost(100)
            completionProgressBar.progress = progress
            completionTextView.text = "$progress% 완료."
        } else {
            completionProgressBar.progress = 0
            completionTextView.text = "목표 시간 없음."
        }


        // 확인 버튼 클릭 시 MainActivity로 돌아가기
        confirmButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}