// com/biofocus/concentration/data/health/PermissionsRationaleActivity.kt
package com.biofocus.data.health

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

/**
 * Health Connect 권한을 왜 요청하는지 설명하는 화면.
 * Health Connect 권한 팝업에서 "자세히 보기"를 눌렀을 때 열림.
 */
class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = """
                이 앱은 학습 중 집중도를 측정하기 위해
                심박수(Heart rate) 데이터를 사용합니다.

                • 삼성헬스 / 웨어러블 기기에서 기록된 심박 데이터만 읽어옵니다.
                • Health Connect를 통해서만 접근하며,
                  서버로 업로드하지 않고 기기 내부에서만 분석에 사용합니다.
                • 언제든지 Health Connect 앱에서 권한을 해제할 수 있습니다.
            """.trimIndent()
            textSize = 16f
            setPadding(40, 80, 40, 40)
        }

        setContentView(textView)
    }
}
