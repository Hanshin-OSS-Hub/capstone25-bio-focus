// com/biofocus/concentration/data/health/HealthConnectPermissionManager.kt
package com.biofocus.data.health

import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthConnectPermissionManager(
    private val activity: ComponentActivity
) {

    // Health Connect 클라이언트
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(activity)
    }

    // 우리가 필요로 하는 권한들 (심박수 읽기)
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    // 권한 요청 런처 (Health Connect 전용 ActivityResultContract 사용)
    private val requestPermissionsLauncher =
        activity.registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->
            // 필요하면 여기서 grantedPermissions 결과 처리
            // (지금은 안 써도 되니까 비워둬도 됨)
        }

    /**
     * 이미 권한을 가지고 있는지 확인
     */
    suspend fun hasPermissions(): Boolean = withContext(Dispatchers.IO) {
        val granted = healthConnectClient
            .permissionController
            .getGrantedPermissions()   // ✅ 인자 없음

        granted.containsAll(permissions)
    }

    /**
     * 권한 요청 다이얼로그(헬스 커넥트 권한 화면) 띄우기
     */
    suspend fun requestPermissions() {
        // PermissionController 전용 ActivityResultContract 사용
        requestPermissionsLauncher.launch(permissions)
    }
}
