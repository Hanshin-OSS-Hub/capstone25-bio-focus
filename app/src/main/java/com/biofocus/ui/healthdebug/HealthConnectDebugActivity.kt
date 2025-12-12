// com/biofocus/concentration/data/health/HealthConnectRepository.kt
package com.biofocus.ui.healthdebug

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectRepository(private val context: Context) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    /**
     * 최근 N분 동안의 평균 심박수 가져오기 (디버그 로그 포함)
     */
    suspend fun getAverageHeartRateLastMinutes(
        minutes: Long = 5L
    ): Double? {
        val end = Instant.now()
        val start = end.minusSeconds(minutes * 60)

        Log.d(TAG, "▶ getAverageHeartRateLastMinutes($minutes) 호출됨")
        Log.d(TAG, "   timeRange: $start ~ $end")

        val allSamples = mutableListOf<HeartRateRecord.Sample>()
        var pageToken: String? = null

        try {
            do {
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(start, end),
                        pageToken = pageToken
                    )
                )

                Log.d(
                    TAG,
                    "   readRecords() → records=${response.records.size}, pageToken=${response.pageToken}"
                )

                response.records.forEachIndexed { index, record ->
                    Log.d(
                        TAG,
                        "   record[$index]: start=${record.startTime}, end=${record.endTime}, samples=${record.samples.size}"
                    )
                    allSamples += record.samples
                }

                pageToken = response.pageToken
            } while (pageToken != null)
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException 발생 (OS/헬스 권한 문제 가능)", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "❌ 기타 예외 발생", e)
            return null
        }

        if (allSamples.isEmpty()) {
            Log.d(TAG, "⚠ allSamples 비어 있음 → 이 시간 범위에 심박 샘플이 없음")
            return null
        }

        val avgBpm = allSamples.map { it.beatsPerMinute }.average()
        Log.d(TAG, "✅ 평균 심박수: $avgBpm bpm (samples=${allSamples.size})")
        return avgBpm
    }

    companion object {
        private const val TAG = "HealthRepo"
    }
}
