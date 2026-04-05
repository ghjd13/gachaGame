package com.example.gachagame

import android.os.Bundle
import android.util.Log
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

    // 전투 진행 상태를 정의합니다.
    private enum class BattleState {
        SELECTING_SKILL, // 스킬(카드) 선택 대기
        SELECTING_TARGET // 스킬 대상(적 또는 아군) 선택 대기
    }

    private var currentState = BattleState.SELECTING_SKILL
    private var selectedCard: View? = null
    private var selectedTarget: View? = null

    // 행동을 마친 캐릭터 수를 셉니다 (최대 3명)
    private var actionCompletedCount = 0
    private val MAX_CHARACTERS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // ========================================================
        // 1. 편성 데이터에 맞춰 아군 이미지 셋팅
        // ========================================================
        val formation = CharacterDataManager.getFormation(this)
        val testFormation = if (formation.size >= 3) formation else listOf(1, 2, 3)

        setCharacterImage(binding.ivAlly1, testFormation[0])
        setCharacterImage(binding.ivAlly2, testFormation[1])
        setCharacterImage(binding.ivAlly3, testFormation[2])

        // ========================================================
        // 2. 카드(스킬) 클릭 이벤트 설정
        // ========================================================
        setupCardInteraction(binding.card1)
        setupCardInteraction(binding.card2)
        setupCardInteraction(binding.card3)

        // ========================================================
        // 3. 타겟(아군/적군) 클릭 이벤트 설정
        // ========================================================
        setupTargetInteraction(binding.ivAlly1, "아군 1")
        setupTargetInteraction(binding.ivAlly2, "아군 2")
        setupTargetInteraction(binding.ivAlly3, "아군 3")
        setupTargetInteraction(binding.layoutEnemy, "적 몬스터")

        // ========================================================
        // 4. 우측 하단 (확인 / 다음 턴) 버튼 초기화 및 이벤트
        // ========================================================
        updateActionButtonUI() // 처음 시작 시 버튼 상태 세팅

        binding.btnNextTurn.setOnClickListener {
            handleActionButtonClick()
        }
    }

    // 캐릭터 이미지 불러오기 (_ld 이미지로 변경)
    private fun setCharacterImage(imageView: ImageView, characterId: Int) {
        val resourceName = "character_${characterId}_ld"
        val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)

        if (resourceId != 0) {
            imageView.setImageResource(resourceId)
        } else {
            imageView.setImageResource(R.drawable.character_1_ld)
            Log.e("BattleActivity", "$resourceName 이미지를 찾을 수 없어 1번 이미지로 대체됨.")
        }
    }

    // 카드(스킬) 클릭 로직
    private fun setupCardInteraction(card: View) {
        card.setOnClickListener {
            // 모든 행동이 끝난 상태("다음 턴" 대기)라면 카드 선택 막기
            if (actionCompletedCount >= MAX_CHARACTERS) {
                Toast.makeText(this, "모든 캐릭터의 행동을 지정했습니다. 다음 턴을 눌러주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (card == selectedCard) {
                // 이미 선택된 카드를 취소하는 경우
                resetCardPosition(card)
                selectedCard = null
                selectedTarget = null // 타겟도 초기화
                currentState = BattleState.SELECTING_SKILL
                Toast.makeText(this, "스킬 선택 취소", Toast.LENGTH_SHORT).show()
            } else {
                // 새로운 카드를 선택하는 경우
                selectedCard?.let { resetCardPosition(it) } // 이전 카드 내리기
                val moveUpPx = -dpToPx(30).toFloat()
                card.animate().translationY(moveUpPx).setDuration(200).start()

                selectedCard = card
                selectedTarget = null // 카드를 바꾸면 타겟도 다시 골라야 함
                currentState = BattleState.SELECTING_TARGET // 다음 단계: 타겟 선택
                Toast.makeText(this, "대상을 지정하세요.", Toast.LENGTH_SHORT).show()
            }
            updateActionButtonUI() // 상태가 변했으니 버튼 업데이트
        }
    }

    // 캐릭터/적 타겟 클릭 로직
    private fun setupTargetInteraction(targetView: View, targetName: String) {
        targetView.setOnClickListener {
            // 스킬을 먼저 골라야 대상을 지정할 수 있음
            if (currentState == BattleState.SELECTING_TARGET) {
                selectedTarget = targetView
                Toast.makeText(this, "$targetName 선택됨. '확인'을 누르세요.", Toast.LENGTH_SHORT).show()
                updateActionButtonUI() // 대상이 지정되었으니 버튼 활성화
            } else if (currentState == BattleState.SELECTING_SKILL && actionCompletedCount < MAX_CHARACTERS) {
                Toast.makeText(this, "먼저 하단의 스킬을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 하단 액션 버튼 클릭 로직 분기
    private fun handleActionButtonClick() {
        if (actionCompletedCount >= MAX_CHARACTERS) {
            // 1. 모든 행동 지정이 끝난 후 '다음 턴'을 누른 경우
            Toast.makeText(this, "적 턴 진행 후 다음 라운드 시작!", Toast.LENGTH_SHORT).show()

            // 턴 리셋
            actionCompletedCount = 0
            currentState = BattleState.SELECTING_SKILL
            selectedCard?.let { resetCardPosition(it) }
            selectedCard = null
            selectedTarget = null

            updateActionButtonUI()

        } else {
            // 2. 캐릭터 하나의 행동을 확정하는 '확인'을 누른 경우
            if (selectedCard != null && selectedTarget != null) {
                actionCompletedCount++
                Toast.makeText(this, "행동 확정! ($actionCompletedCount/$MAX_CHARACTERS)", Toast.LENGTH_SHORT).show()

                // 지정이 끝났으므로 카드와 타겟 초기화
                selectedCard?.let { resetCardPosition(it) }
                selectedCard = null
                selectedTarget = null
                currentState = BattleState.SELECTING_SKILL

                updateActionButtonUI()
            } else {
                Toast.makeText(this, "스킬과 대상을 모두 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 버튼 텍스트 및 활성화 상태(색상 등) 자동 업데이트 함수
    private fun updateActionButtonUI() {
        if (actionCompletedCount >= MAX_CHARACTERS) {
            // 모든 캐릭터 행동 완료 시
            binding.btnNextTurn.text = "다음 턴"
            binding.btnNextTurn.alpha = 1.0f
            binding.btnNextTurn.isEnabled = true
        } else {
            // 행동 지정 중일 때
            binding.btnNextTurn.text = "확인"

            // 카드와 타겟이 모두 선택되어야 '확인' 버튼을 누를 수 있음
            if (selectedCard != null && selectedTarget != null) {
                binding.btnNextTurn.alpha = 1.0f
                binding.btnNextTurn.isEnabled = true
            } else {
                // 아직 스킬이나 대상을 고르지 않았다면 버튼 비활성화 (흐리게 표시)
                binding.btnNextTurn.alpha = 0.5f
                binding.btnNextTurn.isEnabled = false
            }
        }
    }

    // 카드를 원래 자리로 내리는 함수
    private fun resetCardPosition(card: View) {
        card.animate().translationY(0f).setDuration(200).start()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}