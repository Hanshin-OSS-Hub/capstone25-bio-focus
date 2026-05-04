package data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subject: String,
    val title: String,
    val date: String,

    // 사용자가 입력한 목표 공부 시간, 분 단위
    val targetMinutes: Int = 0,

    val isCompleted: Boolean = false,

    // 실제 공부 세션 시간
    val sessionStartMillis: Long? = null,
    val sessionEndMillis: Long? = null,

    // Health Connect에서 다음날 동기화한 HRV(RMSSD)
    val hrvRmssd: Double? = null,
    val hrvSyncedAt: Long? = null
)