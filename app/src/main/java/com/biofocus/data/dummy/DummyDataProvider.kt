// =========================================================================================
// 2. data.dummy 패키지: 더미 데이터 제공용
//    (com.biofocus.concentration.data.dummy)
// =========================================================================================

/**
 * DummyDataProvider: UI 테스트 및 개발 초기에 사용할 더미 데이터를 제공하는 클래스입니다.
 */
class DummyDataProvider {
    /**
     * 더미 세션 기록 목록을 반환합니다.
     */
    fun getDummySessions(): List<SessionRecord> {
        val now = System.currentTimeMillis()
        // 최근 4일간의 더미 기록
        return listOf(
            SessionRecord("sess_001", now - 86400000 * 3, 3600000, 0.85), // 3일 전, 1시간
            SessionRecord("sess_002", now - 86400000 * 2, 7200000, 0.78), // 2일 전, 2시간
            SessionRecord("sess_003", now - 86400000 * 1, 5400000, 0.92), // 1일 전, 1.5시간
            SessionRecord("sess_004", now, 2700000, 0.95) // 오늘, 45분
        )
    }

    /**
     * 더미 목표 시간을 반환합니다. (분 단위)
     */
    fun getTargetTimeMinutes(): Int {
        return 120 // 2시간 목표
    }

    /**
     * 더미 사용자 이름을 반환합니다.
     */
    fun getUsername(): String {
        return "BioFocus User"
    }
}