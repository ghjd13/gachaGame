package com.example.gachagame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gachagame.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding

    // 1. 랜덤으로 띄울 로딩 이미지 목록을 배열로 만듭니다.
    // ※ 주의: R.drawable.로딩이미지이름 형식으로 실제 res/drawable 폴더에 있는 이미지 이름을 넣으세요.
    private val loadingImages = arrayOf(
        R.drawable.bg_loading_image1,
        R.drawable.bg_loading_image2,
        R.drawable.bg_loading_image3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

// 1. 시스템 바 숨기기 (상태 표시줄 + 네비게이션 바 모두)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

// 2. 화면 스와이프 시 일시적으로 나타났다가 다시 사라지는 설정 (Immersive Mode)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // ==========================================
        // [1] 랜덤 배경 이미지 설정
        // ==========================================
        val randomImage = loadingImages.random() // 배열에서 무작위로 하나 뽑기
        binding.imgLoadingBackground.setImageResource(randomImage)

        // ==========================================
        // [2] 목적지 확인 및 딜레이 후 이동
        // ==========================================
        // MainActivity에서 보낸 쪽지(목적지)를 읽어옵니다.
        val destination = intent.getStringExtra("DESTINATION")

        // 2초(2000밀리초) 동안 대기했다가 코드를 실행합니다.
        Handler(Looper.getMainLooper()).postDelayed({

            // 목적지에 따라 다음 화면으로 넘어갑니다.
            when (destination) {
                "SUMMON" -> {
                     val nextIntent = Intent(this, SummonActivity::class.java)
                     startActivity(nextIntent)
                }
                "BATTLE" -> {
                    // TODO: 전투 화면(BattleActivity)을 만들면 주석을 해제하세요!
                    // val nextIntent = Intent(this, BattleActivity::class.java)
                    // startActivity(nextIntent)
                }
            }

            // 중요: 다음 화면으로 넘어간 뒤에는 로딩창을 닫아줍니다.
            // (그래야 뒤로가기를 눌렀을 때 다시 로딩창이 안 나옵니다)
            finish()

        }, 2000) // 2000 = 2초, 3000 = 3초 (원하는 대기 시간으로 수정하세요)
    }
}