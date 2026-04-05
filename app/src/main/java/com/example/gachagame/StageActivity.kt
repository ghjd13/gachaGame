package com.example.gachagame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage)

        val btnStage1: Button = findViewById(R.id.btn_stage_1)

        // 스테이지 1 버튼 클릭 시 전투 화면으로 이동
        btnStage1.setOnClickListener {
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("DESTINATION", "BATTLE")

            // 나중에 스테이지 1-1의 맵 데이터, 적 데이터 등을 전달해야 한다면 여기에 추가할 수 있습니다.
            // intent.putExtra("STAGE_ID", "1-1")

            startActivity(intent)
        }
    }
}