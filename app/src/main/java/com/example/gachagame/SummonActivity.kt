package com.example.gachagame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gachagame.databinding.ActivitySummonBinding

class SummonActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummonBinding

    // ⭐ [추가됨] 1. 현재 선택된 탭이 무엇인지 기억하는 변수 (처음 켜면 픽업이 기본이니까 "PICKUP"으로 시작)
    private var currentSummonType = "PICKUP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // 1. 시스템 바 숨기기 (상태 표시줄 + 네비게이션 바 모두)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // 2. 화면 스와이프 시 일시적으로 나타났다가 다시 사라지는 설정 (Immersive Mode)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 화면이 처음 켜졌을 때: 픽업 소환 탭이 기본으로 선택된 상태(40dp)로 설정
        setTabSelected(binding.tabPickupSummon, binding.tabNormalSummon)

        // 뒤로 가기 버튼 클릭 이벤트 (현재 액티비티를 종료하고 이전 화면으로 돌아감)
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 1회 소환 버튼
        binding.btnSummon1.setOnClickListener {
            val intent = Intent(this, SummonResultActivity::class.java)
            intent.putExtra("PULL_COUNT", 1) // 1개만 떨어지게 설정
            // ⭐ [추가됨] 3. 소환 버튼을 누를 때 현재 상태를 결과창으로 같이 보냅니다.
            intent.putExtra("SUMMON_TYPE", currentSummonType)
            startActivity(intent)
        }

        // 10회 소환 버튼
        binding.btnSummon10.setOnClickListener {
            val intent = Intent(this, SummonResultActivity::class.java)
            intent.putExtra("PULL_COUNT", 10) // 10개가 떨어지게 설정
            // ⭐ [추가됨] 3. 소환 버튼을 누를 때 현재 상태를 결과창으로 같이 보냅니다.
            intent.putExtra("SUMMON_TYPE", currentSummonType)
            startActivity(intent)
        }

        // 픽업 소환 탭 클릭 이벤트
        binding.tabPickupSummon.setOnClickListener {
            // [추가됨] 2. 픽업 탭을 눌렀으니 상태를 "PICKUP"으로 변경
            currentSummonType = "PICKUP"

            // 픽업 탭을 40dp로, 일반 탭을 30dp로 변경
            setTabSelected(binding.tabPickupSummon, binding.tabNormalSummon)

            binding.imgSummonBanner.setImageResource(R.drawable.summon_banner_pickup)
        }

        // 일반 소환 탭 클릭 이벤트
        binding.tabNormalSummon.setOnClickListener {
            // [추가됨] 2. 일반 탭을 눌렀으니 상태를 "NORMAL"로 변경
            currentSummonType = "NORMAL"

            // 일반 탭을 40dp로, 픽업 탭을 30dp로 변경
            setTabSelected(binding.tabNormalSummon, binding.tabPickupSummon)

            binding.imgSummonBanner.setImageResource(R.drawable.summon_banner_normal)
        }
    }

    // ==========================================
    // [핵심 로직] 선택된 탭의 마진을 조절하는 함수
    // ==========================================
    private fun setTabSelected(selectedTab: View, unselectedTab: View) {
        // 1. 선택된 탭: start 마진을 40dp로 설정 (앞으로 튀어나오는 효과)
        val selectedParams = selectedTab.layoutParams as ConstraintLayout.LayoutParams
        selectedParams.marginStart = dpToPx(40)
        selectedTab.layoutParams = selectedParams

        // 2. 선택되지 않은 탭: start 마진을 다시 30dp로 원상복구
        val unselectedParams = unselectedTab.layoutParams as ConstraintLayout.LayoutParams
        unselectedParams.marginStart = dpToPx(30)
        unselectedTab.layoutParams = unselectedParams
    }

    // 안드로이드 코드에서 dp 단위를 사용하기 위해 픽셀(px)로 변환해주는 유틸리티 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}