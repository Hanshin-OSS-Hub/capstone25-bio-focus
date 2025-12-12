// =========================================================================================
// 3. network 패키지: 나중에 Python 서버 연동을 위한 API 인터페이스
//    (com.biofocus.concentration.network)
// =========================================================================================

/**
 * ConcentrationApi: 백엔드 서버와의 통신을 위한 API 인터페이스입니다. (예: Retrofit 인터페이스)
 */
interface ConcentrationApi {
    // API 통신을 통해 모든 세션 기록을 가져오는 함수
    // 실제로는 suspend 함수로 정의하여 비동기 처리
    suspend fun getSessionRecords(): List<SessionRecord>

    // 새로운 세션 기록을 서버에 저장하는 함수
    suspend fun saveSessionRecord(record: SessionRecord): Boolean

    // 사용자 프로필 정보를 업데이트하는 함수
    suspend fun updateProfile(username: String, targetTime: Int): Boolean
}