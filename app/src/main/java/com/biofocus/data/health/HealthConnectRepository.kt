// com/biofocus/data/health/HealthConnectRepository.kt
package com.biofocus.data.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime

class HealthConnectRepository(private val context: Context) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    companion object {
        private const val TAG = "HealthRepo"
    }

    /**
     * 최근 N분 동안의 평균 심박수 가져오기 (읽기 전용 + 디버그 로그)
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
                    "   readRecords() → records=${response.records.size}, " +
                            "pageToken=${response.pageToken}"
                )

                response.records.forEachIndexed { index, record ->
                    Log.d(
                        TAG,
                        "   record[$index]: start=${record.startTime}, " +
                                "end=${record.endTime}, samples=${record.samples.size}"
                    )
                    allSamples += record.samples
                }

                pageToken = response.pageToken
            } while (pageToken != null)
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException (심박 READ 권한 문제 가능)", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "❌ 기타 예외", e)
            return null
        }

        if (allSamples.isEmpty()) {
            Log.d(TAG, "⚠ allSamples 비어 있음 → 이 시간 범위에 심박 샘플이 없음")
            return null
        }

        // 1.2.0 에서 beatsPerMinute 는 Long 이라 Double 로 변환
        val avgBpm = allSamples
            .map { it.beatsPerMinute.toDouble() }
            .average()

        Log.d(TAG, "✅ 평균 심박수: $avgBpm bpm (samples=${allSamples.size})")
        return avgBpm
    }

    /**
     * 오늘(00:00~지금) 사이에서 "가장 최근 심박 샘플" 한 개 가져오기
     * (최근 N분이 비었을 때 fallback 용)
     */
    suspend fun getLatestHeartRateToday(): Double? {
        val nowZoned = ZonedDateTime.now()
        val startOfDay = nowZoned.toLocalDate()
            .atStartOfDay(nowZoned.zone)
            .toInstant()
        val end = nowZoned.toInstant()

        Log.d(TAG, "▶ getLatestHeartRateToday() 호출됨")
        Log.d(TAG, "   timeRange(today): $startOfDay ~ $end")

        val allSamples = mutableListOf<HeartRateRecord.Sample>()
        var pageToken: String? = null

        try {
            do {
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, end),
                        pageToken = pageToken
                    )
                )

                Log.d(
                    TAG,
                    "   readRecords(today) → records=${response.records.size}, " +
                            "pageToken=${response.pageToken}"
                )

                response.records.forEachIndexed { index, record ->
                    Log.d(
                        TAG,
                        "   todayRecord[$index]: start=${record.startTime}, " +
                                "end=${record.endTime}, samples=${record.samples.size}"
                    )
                    allSamples += record.samples
                }

                pageToken = response.pageToken
            } while (pageToken != null)
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ getLatestHeartRateToday: SecurityException", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "❌ getLatestHeartRateToday: 기타 예외", e)
            return null
        }

        if (allSamples.isEmpty()) {
            Log.d(TAG, "⚠ 오늘 하루 전체에서도 심박 샘플이 없음")
            return null
        }

        val latestSample = allSamples.maxByOrNull { it.time } ?: return null
        val bpm = latestSample.beatsPerMinute.toDouble()

        Log.d(
            TAG,
            "✅ 오늘 가장 최근 심박: $bpm bpm, time=${latestSample.time}"
        )
        return bpm
    }

    /**
     * 디버그용: 1970년 이후 모든 HeartRateRecord 를 읽어서
     * 개수/일부 내용을 로그로 출력.
     */
    suspend fun debugDumpAllHeartRates(maxPrint: Int = 50) {
        Log.d(TAG, "▶ debugDumpAllHeartRates() 호출됨")

        val allRecords = mutableListOf<HeartRateRecord>()
        var pageToken: String? = null

        try {
            do {
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.after(Instant.EPOCH),
                        pageToken = pageToken
                    )
                )

                allRecords += response.records
                pageToken = response.pageToken
            } while (pageToken != null)
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ debugDumpAllHeartRates: SecurityException", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "❌ debugDumpAllHeartRates: 기타 예외", e)
            return
        }

        Log.d(TAG, "🔍 전체 HeartRateRecord 개수 = ${allRecords.size}")

        if (allRecords.isEmpty()) {
            Log.w(TAG, "⚠ HeartRateRecord 자체가 한 개도 없음")
            return
        }

        val sorted = allRecords.sortedBy { it.startTime }
        val first = sorted.first()
        val last = sorted.last()

        Log.d(
            TAG,
            "   가장 이른 레코드: start=${first.startTime}, " +
                    "end=${first.endTime}, samples=${first.samples.size}"
        )
        Log.d(
            TAG,
            "   가장 최근 레코드: start=${last.startTime}, " +
                    "end=${last.endTime}, samples=${last.samples.size}"
        )

        sorted.take(maxPrint).forEachIndexed { index, record ->
            Log.d(
                TAG,
                "   [$index] start=${record.startTime}, " +
                        "end=${record.endTime}, samples=${record.samples.size}"
            )
        }
    }
}
