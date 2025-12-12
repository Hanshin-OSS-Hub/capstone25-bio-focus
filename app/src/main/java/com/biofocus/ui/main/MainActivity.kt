// com/biofocus/concentration/ui/main/MainActivity.kt
package com.biofocus.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import com.biofocus.concentration.R
import com.biofocus.data.health.HealthConnectPermissionManager
import com.biofocus.ui.profile.ProfileActivity
import com.biofocus.ui.study.StudyActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    // 🔹 메인 화면 UI 뷰들
    private lateinit var profileImage: ImageView
    private lateinit var dateTextView: TextView
    private lateinit var editTextHour: EditText
    private lateinit var editTextMinute: EditText
    private lateinit var startButton: Button

    // 🔹 Health Connect 권한 상태 확인용
    private lateinit var permissionManager: HealthConnectPermissionManager

    // 🔹 “권한 팝업을 이미 보여줬는지” 기억하는 SharedPreferences
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) 메인 화면 UI 초기화
        profileImage = findViewById(R.id.profile_image_main)
        dateTextView = findViewById(R.id.date_text_view)
        editTextHour = findViewById(R.id.edit_text_hour)
        editTextMinute = findViewById(R.id.edit_text_minute)
        startButton = findViewById(R.id.start_button)

        // 2) 현재 날짜 표시
        val sdf = SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREA)
        val currentDate = sdf.format(Date())
        dateTextView.text = currentDate

        // 3) 프로필 이미지 클릭 시 ProfileActivity로 이동
        profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // 4) 시작 버튼 클릭 시 StudyActivity로 이동
        startButton.setOnClickListener {
            val targetHour = editTextHour.text.toString().toIntOrNull() ?: 0
            val targetMinute = editTextMinute.text.toString().toIntOrNull() ?: 0
            val totalTargetMinutes = targetHour * 60 + targetMinute

            val intent = Intent(this, StudyActivity::class.java).apply {
                putExtra("TARGET_MINUTES", totalTargetMinutes)
            }
            startActivity(intent)
        }

        // 5) Health Connect 권한 체크 매니저 초기화
        permissionManager = HealthConnectPermissionManager(this)

        // 6) 앱 첫 실행 시 Health Connect 권한 팝업 한 번 띄우기
        maybeShowHealthPermissionDialog()
    }

    /**
     * 권한이 없고, 아직 한 번도 팝업을 안 보여줬다면 다이얼로그 띄움
     */
    private fun maybeShowHealthPermissionDialog() {
        lifecycleScope.launch {
            val alreadyAsked = prefs.getBoolean("health_permission_asked", false)
            val hasPerm = permissionManager.hasPermissions()

            if (!hasPerm && !alreadyAsked) {
                showHealthPermissionDialog()
            }
        }
    }

    /**
     * “집중도 측정을 위해 Health Connect 권한이 필요합니다” 팝업
     */
    private fun showHealthPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Health Connect 권한 요청")
            .setMessage(
                "집중도 측정을 위해 삼성헬스에 기록된 심박수 데이터를 " +
                        "Health Connect를 통해 읽어야 합니다.\n\n지금 권한을 설정하시겠습니까?"
            )
            .setPositiveButton("지금 허용") { _, _ ->
                // 다음부턴 자동으로 안 뜨게 표시
                prefs.edit().putBoolean("health_permission_asked", true).apply()

                // ✅ 여기서 실제로 Health Connect 권한 요청 (앱을 Health Connect에 등록)
                lifecycleScope.launch {
                    permissionManager.requestPermissions()
                }
            }
            .setNegativeButton("나중에") { _, _ ->
                // 나중에 누르면 자동 팝업은 더 이상 안 뜨고,
                // 프로필 화면에서 직접 권한 설정 버튼으로만 진입 가능
                prefs.edit().putBoolean("health_permission_asked", true).apply()
            }
            .show()
    }

    /**
     * Health Connect 설정 화면 열기
     * - 필요하면 다른 버튼에서 호출해서 쓸 수 있음
     */
    private fun openHealthConnectSettings() {
        // Health Connect 설정 메인 화면으로 이동
        val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "Health Connect 앱을 찾을 수 없습니다. Play 스토어 / 시스템 설정에서 확인해 주세요.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
