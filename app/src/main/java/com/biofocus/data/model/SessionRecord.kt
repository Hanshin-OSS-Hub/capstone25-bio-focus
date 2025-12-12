// =========================================================================================
// 1. data.model 패키지: 화면에서 사용하는 데이터 클래스
//    (com.biofocus.concentration.data.model)
// =========================================================================================

/**
 * 하나의 집중 세션 기록을 저장하는 데이터 클래스.
 * SessionRecord: 공부 세션 기록 모델
 * @param startTime 세션 시작 시간 (Unix time, milliseconds)
 * @param durationMillis 집중 시간 (milliseconds)
 * @param concentrationScore 집중도 점수 (예: 0.0 ~ 1.0)
 */
data class SessionRecord(
    val id: String,
    val startTime: Long,
    val durationMillis: Long,
    val concentrationScore: Double
)