com.biofocus \br
├─ data 
     UI와 분리된 순수 데이터 처리 영역
│  ├─ dummy
│  │  └─ DummyDataProvider.kt 
	     테스트용 데이터 생성,가짜 공부 세션, 사용자 이름, 목표 시간 제공
│  ├─ health
│  │  ├─ HealthConnectPermissionManager.kt 
	     Health Connect 심박수 권한이 있는지 확인
│  │  ├─ HealthConnectRepository.kt
	     Health Connect API를 통해 심박수 데이터 조회
│  │  └─ PermissionsRationaleActivity.kt
	     심박수 사용 목적 및 개인정보 처리 방식 안내
│  └─ model
│     └─ SessionRecord.kt
	    세션 시작 시간, 공부 시간, 집중도 점수를 하나의 구조로 관리
│
├─ network
│  └─ ConcentrationApi.kt
	 서버(FastAPI 등)와 통신하기 위한 API 인터페이스 정의
	 공부 세션 조회 / 저장 / 프로필 업데이트 기능 예정
│
├─ ui
     사용자가 실제로 보는 화면들
│  ├─ main
│  │  └─ MainActivity.kt
	     앱 실행 시 가장 먼저 보이는 메인 화면
│  ├─ study
│  │  ├─ StudyActivity.kt
	     공부 진행 화면
	     Health Connect 심박수 주기적 수집
│  │  └─ StudyEndActivity.kt
	     공부 종료 후 결과 요약 화면
│  ├─ profile
│  │  └─ ProfileActivity.kt
	     사용자 프로필 정보 표시
	     Health Connect 권한 상태 확인 및 재설정
│  ├─ calendar
│  │  └─ CalendarActivity.kt
	     날짜별 공부 기록을 달력 형태로 표시
	     특정 날짜 클릭 시 세션 기록 확인 (예정)
│  └─ healthdebug
│     └─ HealthConnectDebugActivity.kt
	    개발 중 Health Connect 권한 상태 테스트 (실사용자용 아님)
│
└─ res
   └─ layout
      ├─ activity_main.xml   메인 화면 UI
      ├─ activity_study.xml   공부 진행 화면 UI
      ├─ activity_study_end.xml   공부 종료 요약 UI
      ├─ activity_profile.xml   프로필/설정 화면 UI
      └─ activity_health_debug.xml   헬스커넥트 테스트 UI
