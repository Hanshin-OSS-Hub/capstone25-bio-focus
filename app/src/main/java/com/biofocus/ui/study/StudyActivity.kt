// com/biofocus/concentration/ui/study/StudyActivity.kt
package com.biofocus.ui.study

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.lifecycle.lifecycleScope
import com.biofocus.concentration.R
import com.biofocus.data.health.HealthConnectRepository
import com.biofocus.ui.profile.ProfileActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class StudyActivity : ComponentActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var timerDisplay: TextView
    private lateinit var remainingTimerDisplay: TextView
    private lateinit var concentrationGauge: ProgressBar
    private lateinit var concentrationStatusText: TextView
    private lateinit var distractionStatusText: TextView
    private lateinit var endButton: Button

    // 공부 시간 관련
    private var studyDurationMillis: Long = 0L
    private var targetDurationMillis: Long = 0L
    private var isStudying: Boolean = false

    // 타이머용 Handler
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isStudying) {
                studyDurationMillis += 1000L
                updateTimerDisplay()
                updateRemainingTimerDisplay()
                handler.postDelayed(this, 1000L)
            }
        }
    }

    // Health Connect 관련
    private lateinit var healthRepository: HealthConnectRepository
    private lateinit var healthConnectClient: HealthConnectClient
    private var heartRateJob: Job? = null

    // Health Connect 에서 요청할 권한(심박 읽기)
    private val HC_PERMISSIONS: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    // 권한 요청 런처 (공식 가이드 패턴)
    private val requestHealthConnectPermissionsLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            if (granted.containsAll(HC_PERMISSIONS)) {
                // 권한 허용됨 → 심박 업데이트 시작
                startHeartRateUpdates()
            } else {
                distractionStatusText.text = "Health Connect 심박수 권한이 거부되었습니다."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        // 뷰 바인딩
        profileImage = findViewById(R.id.profile_image_study)
        timerDisplay = findViewById(R.id.timer_display)
        remainingTimerDisplay = findViewById(R.id.remaining_timer_display)
        concentrationGauge = findViewById(R.id.concentration_gauge)
        concentrationStatusText = findViewById(R.id.concentration_status_text)
        distractionStatusText = findViewById(R.id.distraction_status_text)
        endButton = findViewById(R.id.end_button)

        // Health Connect 객체
        healthRepository = HealthConnectRepository(this)
        healthConnectClient = HealthConnectClient.getOrCreate(this)

        // MainActivity 등에서 넘겨준 목표 시간(분)
        val targetMinutes = intent.getIntExtra("TARGET_MINUTES", 0)
        targetDurationMillis = targetMinutes * 60 * 1000L

        // 프로필 클릭 → ProfileActivity 이동
        profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 종료 버튼 클릭 → StudyEndActivity 이동
        endButton.setOnClickListener {
            stopStudy()
            val intent = Intent(this, StudyEndActivity::class.java).apply {
                putExtra("STUDY_DURATION_MILLIS", studyDurationMillis)
                putExtra("TARGET_DURATION_MILLIS", targetDurationMillis)
            }
            startActivity(intent)
            finish()
        }

        // 화면 켜지면 바로 공부 시작
        startStudy()
    }

    /**
     * 공부 시작:
     * - 타이머 시작
     * - Health Connect 권한 체크 & 없으면 요청
     */
    private fun startStudy() {
        isStudying = true
        studyDurationMillis = 0L
        handler.post(updateRunnable)

        Log.d("Study", "▶ startStudy() 호출됨")

        lifecycleScope.launch {
            Log.d("Study", "▶ checkHealthConnectPermissionsAndStart() 진입")
            checkHealthConnectPermissionsAndStart()
        }
    }

    /**
     * Health Connect 권한 확인 & 없으면 권한 화면 띄우기
     */
    private suspend fun checkHealthConnectPermissionsAndStart() {
        val granted = healthConnectClient
            .permissionController
            .getGrantedPermissions()

        if (granted.containsAll(HC_PERMISSIONS)) {
            // 이미 권한 있음 → 심박 업데이트 시작
            startHeartRateUpdates()
        } else {
            // 권한 요청 (헬스 커넥트 권한 화면 뜸)
            withContext(Dispatchers.Main) {
                distractionStatusText.text = "심박수 권한을 요청하는 중입니다..."
            }
            requestHealthConnectPermissionsLauncher.launch(HC_PERMISSIONS)
        }
    }

    /**
     * 공부 종료:
     * - 타이머 중지
     * - 심박 업데이트 중지
     */
    private fun stopStudy() {
        isStudying = false
        handler.removeCallbacks(updateRunnable)
        heartRateJob?.cancel()
    }

    /**
     * 일정 주기(예: 30초)에 한 번씩 Health Connect에서
     * 최근 3분 평균 심박수를 읽어서 집중도/심박 텍스트를 갱신
     */
    private fun startHeartRateUpdates() {
        heartRateJob?.cancel()

        heartRateJob = lifecycleScope.launch {
            Log.d("Study", "▶ startHeartRateUpdates() 시작")

            while (isStudying) {
                Log.d("Study", "▶ 심박 읽기 루프 진입")

                // 백그라운드에서 Health Connect 호출
                val bpm = withContext(Dispatchers.IO) {
                    Log.d("Study", "▶ Repository 호출 직전 (최근 3분 평균)")
                    // 1) 먼저 최근 3분 평균 시도
                    val avg = healthRepository.getAverageHeartRateLastMinutes(3)

                    if (avg != null) {
                        Log.d("Study", "▶ 최근 3분 평균 심박 = $avg")
                        avg
                    } else {
                        Log.d("Study", "▶ 최근 3분 데이터 없음, 오늘 최신값 fallback 시도")
                        // 2) 없으면 오늘 전체에서 가장 최근 심박 하나 가져오기
                        healthRepository.getLatestHeartRateToday()
                    }
                }

                Log.d("Study", "▶ Repository 결과 bpm = $bpm")

                // UI 업데이트는 메인 스레드에서
                withContext(Dispatchers.Main) {
                    if (!isStudying) return@withContext

                    if (bpm != null) {
                        // 레이아웃에는 heartRateText 가 없어서 distractionStatusText 를 재사용
                        distractionStatusText.text =
                            String.format(Locale.getDefault(), "심박수: %.0f BPM", bpm)
                    } else {
                        distractionStatusText.text = "최근 심박 데이터가 없습니다"
                    }
                }

                // 30초마다 갱신 (원하면 주기 조절 가능)
                repeat(5) {
                    if (!isStudying) return@launch
                    delay(1000)
                }
            }
        }
    }

    /**
     * 심박수 → 0~100 집중도 점수로 단순 매핑
     */
    private fun mapHeartRateToConcentration(hr: Double): Int {
        val minHr = 60.0
        val maxHr = 120.0
        val clamped = hr.coerceIn(minHr, maxHr)
        val ratio = (clamped - minHr) / (maxHr - minHr)
        val score = (ratio * 100).toInt()
        return score.coerceIn(0, 100)
    }

    /**
     * 경과 시간(공부 시간) 표시
     */
    private fun updateTimerDisplay() {
        val seconds = (studyDurationMillis / 1000) % 60
        val minutes = (studyDurationMillis / (1000 * 60)) % 60
        val hours = (studyDurationMillis / (1000 * 60 * 60))
        timerDisplay.text = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }

    /**
     * 남은 시간 표시
     */
    private fun updateRemainingTimerDisplay() {
        if (targetDurationMillis <= 0L) {
            remainingTimerDisplay.text = "남은 시간: --:--"
            return
        }

        val remainingMillis = (targetDurationMillis - studyDurationMillis).coerceAtLeast(0L)
        val seconds = (remainingMillis / 1000) % 60
        val minutes = (remainingMillis / (1000 * 60))

        remainingTimerDisplay.text = String.format(
            Locale.getDefault(),
            "남은 시간: %02d:%02d",
            minutes,
            seconds
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        heartRateJob?.cancel()
    }
}
