package health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val client by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)
    )

    fun isAvailable(): Boolean {
        val providerPackageName = "com.google.android.apps.healthdata"
        val status = HealthConnectClient.getSdkStatus(context, providerPackageName)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasAllPermissions(): Boolean {
        val grantedPermissions = client.permissionController.getGrantedPermissions()
        return grantedPermissions.containsAll(permissions)
    }

    suspend fun readAverageHrvRmssd(
        startMillis: Long,
        endMillis: Long
    ): Double? {
        if (endMillis <= startMillis) return null

        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(startMillis),
                    Instant.ofEpochMilli(endMillis)
                )
            )
        )

        val values = response.records.map { it.heartRateVariabilityMillis }
        if (values.isEmpty()) return null

        return values.average()
    }
}