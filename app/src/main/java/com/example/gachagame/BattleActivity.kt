package com.example.gachagame

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gachagame.databinding.ActivityBattleBinding

class BattleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleBinding

    // 현재 선택된 카드를 기억하는 변수
    private var selectedCard: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 전체화면 (Immersive Mode) 설정
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 2. 카드 클릭 이벤트 연결
        setupCardInteraction(binding.card1)
        setupCardInteraction(binding.card2)
        setupCardInteraction(binding.card3)

        // 3. 다음 턴 버튼 클릭 이벤트
        binding.btnNextTurn.setOnClickListener {
            Toast.makeText(this, "다음 턴으로 넘어갑니다.", Toast.LENGTH_SHORT).show()
            // 턴이 넘어가면 선택된 카드를 원상복구시킵니다.
            selectedCard?.animate()?.translationY(0f)?.setDuration(200)?.start()
            selectedCard = null
        }
    }

    // 카드를 클릭했을 때 30dp 위로 올라가고, 재클릭 시 취소되는 로직
    private fun setupCardInteraction(card: View) {
        card.setOnClickListener {
            // 이미 이 카드가 위로 올라가 있는 상태라면? (선택 취소)
            if (card == selectedCard) {
                card.animate().translationY(0f).setDuration(200).start()
                selectedCard = null
            } else {
                // 다른 카드가 선택되어 있었다면 먼저 아래로 내림
                selectedCard?.animate()?.translationY(0f)?.setDuration(200)?.start()

                // 클릭한 카드를 30dp 위로 올림 (Y축은 위로 갈수록 마이너스 값)
                val moveUpPx = -dpToPx(30).toFloat()
                card.animate().translationY(moveUpPx).setDuration(200).start()

                // 현재 카드를 '선택된 카드'로 기억
                selectedCard = card
            }
        }
    }

    // dp를 픽셀(px)로 변환해주는 도구 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}