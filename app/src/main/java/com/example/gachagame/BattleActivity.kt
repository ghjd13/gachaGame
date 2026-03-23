package com.example.gachagame

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gachagame.databinding.ActivityBattleBinding

class BattleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleBinding
    private var selectedCard: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // ========================================================
        // [추가된 부분] 편성된 캐릭터 데이터를 불러와서 이미지 적용
        // ========================================================
        val formation = CharacterDataManager.getFormation(this)

        // formation 리스트에 저장된 아이디(예: [1, 2, 3])를 이용해 이미지 교체
        if (formation.size >= 1) setCharacterImage(binding.ivAlly1, formation[0])
        if (formation.size >= 2) setCharacterImage(binding.ivAlly2, formation[1])
        if (formation.size >= 3) setCharacterImage(binding.ivAlly3, formation[2])
        // ========================================================

        // 카드 및 버튼 이벤트 세팅
        setupCardInteraction(binding.card1)
        setupCardInteraction(binding.card2)
        setupCardInteraction(binding.card3)

        binding.btnNextTurn.setOnClickListener {
            Toast.makeText(this, "다음 턴으로 넘어갑니다.", Toast.LENGTH_SHORT).show()
            selectedCard?.animate()?.translationY(0f)?.setDuration(200)?.start()
            selectedCard = null
        }
    }

    // 캐릭터 ID를 받아서 "character_{id}_ld" 형식의 이미지를 찾아 뷰에 넣는 함수
    private fun setCharacterImage(imageView: ImageView, characterId: Int) {
        val resourceName = "character_${characterId}_ld"

        // 이름(String)으로 리소스 아이디(Int)를 찾아내는 마법의 코드
        val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)

        if (resourceId != 0) {
            imageView.setImageResource(resourceId)
        } else {
            // 혹시 이미지를 못 찾으면(예: 21번을 달라고 하면) 기본 1번 이미지를 넣습니다.
            imageView.setImageResource(R.drawable.character_1_ld)
        }
    }

    private fun setupCardInteraction(card: View) {
        card.setOnClickListener {
            if (card == selectedCard) {
                card.animate().translationY(0f).setDuration(200).start()
                selectedCard = null
            } else {
                selectedCard?.animate()?.translationY(0f)?.setDuration(200)?.start()
                val moveUpPx = -dpToPx(30).toFloat()
                card.animate().translationY(moveUpPx).setDuration(200).start()
                selectedCard = card
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}