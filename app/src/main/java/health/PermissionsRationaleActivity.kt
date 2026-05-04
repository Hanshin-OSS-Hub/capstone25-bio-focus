package health

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PermissionsRationaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = """
                이 앱은 전날 공부 시간대의 심박변이도(HRV) 데이터를 확인하기 위해
                Health Connect 읽기 권한을 사용합니다.

                사용자가 허용한 범위의 데이터만 읽으며,
                공부 기록 분석 목적으로만 사용됩니다.
            """.trimIndent()
            textSize = 16f
            setPadding(48, 96, 48, 96)
        }

        setContentView(textView)
    }
}