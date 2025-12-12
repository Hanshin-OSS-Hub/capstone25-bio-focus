// com/biofocus/concentration/ui/profile/ProfileActivity.kt
package com.biofocus.ui.profile

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.lifecycle.lifecycleScope
import com.biofocus.concentration.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileActivity : ComponentActivity() {

    // ----- UI -----
    private lateinit var monthYearDisplay: TextView
    private lateinit var summaryProgressBar: ProgressBar
    private lateinit var summaryProgressText: TextView
    private lateinit var totalStudyTimeText: TextView
    private lateinit var averageConcentrationTimeText: TextView
    private lateinit var healthPermissionStatusText: TextView
    private lateinit var healthPermissionButton: Button

    // ----- Health Connect -----
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(this)
    }

    // OS 레벨 심박수 권한 (문자열로 직접 지정)
    private val OS_HEART_PERMISSION = "android.permission.health.READ_HEART_RATE"

    // Health Connect(앱 내부) 권한
    private val healthPermissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    // 1단계: OS 런타임 건강 권한 요청
    private val requestOsHealthPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // OS 권한 허용 → 이제 Health Connect 권한 요청
                requestHealthConnectPermissions()
            } else {
                Toast.makeText(
                    this,
                    "시스템 건강 권한(심박수)이 거부되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                lifecycleScope.launch { updatePermissionStatus() }
            }
        }

    // 2단계: Health Connect 권한 요청
    private val requestHealthConnectPermissionsLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->
            lifecycleScope.launch {
                updatePermissionStatus()
                if (grantedPermissions.containsAll(healthPermissions)) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Health Connect 권한이 허용되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Health Connect 권한이 거부되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ----- UI 바인딩 -----
        monthYearDisplay = findViewById(R.id.month_year_display)
        summaryProgressBar = findViewById(R.id.summary_progress_bar)
        summaryProgressText = findViewById(R.id.summary_progress_text)
        totalStudyTimeText = findViewById(R.id.total_study_time_text)
        averageConcentrationTimeText = findViewById(R.id.average_concentration_time_text)
        healthPermissionStatusText = findViewById(R.id.txt_health_connect_status)
        healthPermissionButton = findViewById(R.id.btn_health_connect_permission)

        // 상단 월/년도 표시
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
        monthYearDisplay.text = sdf.format(cal.time)

        // 처음 들어올 때 현재 권한 상태 표시
        lifecycleScope.launch {
            updatePermissionStatus()
        }

        // 버튼 눌렀을 때: OS 권한 → Health Connect 권한 순서로 처리
        healthPermissionButton.setOnClickListener {
            lifecycleScope.launch {
                handlePermissionsFlow()
            }
        }
    }

    /**
     * 전체 권한 흐름:
     * 1) OS 건강 권한 체크/요청
     * 2) Health Connect 권한 체크/요청
     */
    private suspend fun handlePermissionsFlow() {
        // 1. OS 심박수 권한부터 확인
        val hasOsHeartPermission = ContextCompat.checkSelfPermission(
            this,
            OS_HEART_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasOsHeartPermission) {
            // 아직 OS 권한이 없으면, 먼저 이걸 요청
            requestOsHealthPermissionLauncher.launch(OS_HEART_PERMISSION)
            return
        }

        // 2. OS 권한은 이미 있음 → 이제 Health Connect 권한 확인/요청
        val hasHealthConnectPerms = hasAllHealthConnectPermissions()
        if (hasHealthConnectPerms) {
            Toast.makeText(
                this,
                "이미 모든 권한이 허용되어 있습니다.",
                Toast.LENGTH_SHORT
            ).show()
            updatePermissionStatus()
        } else {
            // 여기서 Health Connect 권한 화면 뜸
            requestHealthConnectPermissions()
        }
    }

    private fun requestHealthConnectPermissions() {
        requestHealthConnectPermissionsLauncher.launch(healthPermissions)
    }

    /**
     * Health Connect에 우리가 요청한 권한들이 모두 허용되었는지 확인
     */
    private suspend fun hasAllHealthConnectPermissions(): Boolean =
        withContext(Dispatchers.IO) {
            val granted = healthConnectClient
                .permissionController
                .getGrantedPermissions()
            granted.containsAll(healthPermissions)
        }

    /**
     * 화면에 "허용됨 / 미허용" 텍스트 업데이트
     * (OS 권한 + Health Connect 권한 둘 다 만족해야 ✅)
     */
    private suspend fun updatePermissionStatus() {
        val hasOsHeartPermission = ContextCompat.checkSelfPermission(
            this,
            OS_HEART_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

        val hasHealthConnectPerms = hasAllHealthConnectPermissions()

        healthPermissionStatusText.text = when {
            !hasOsHeartPermission ->
                "Health Connect 권한 상태: ❌ 시스템 심박수 권한 없음"
            !hasHealthConnectPerms ->
                "Health Connect 권한 상태: ❌ Health Connect 권한 없음"
            else ->
                "Health Connect 권한 상태: ✅ 모두 허용됨"
        }
    }
}
