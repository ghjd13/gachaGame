package com.example.gachagame

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gachagame.databinding.ActivitySummonResultBinding

class SummonResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummonResultBinding
    private var isRevealed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummonResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

// 1. 시스템 바 숨기기 (상태 표시줄 + 네비게이션 바 모두)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

// 2. 화면 스와이프 시 일시적으로 나타났다가 다시 사라지는 설정 (Immersive Mode)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 이전 화면(SummonActivity)에서 몇 회 소환인지 받아옵니다. (기본값 1)
        val pullCount = intent.getIntExtra("PULL_COUNT", 1)

        // [추가됨] 이전 화면에서 소환 종류(픽업/일반)를 받아옵니다. (안 보내면 기본값 "NORMAL")
        val summonType = intent.getStringExtra("SUMMON_TYPE") ?: "NORMAL"

        // 화면 높이를 구합니다 (위에서 떨어지는 애니메이션을 위해)
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // 1회 또는 10회만큼 캐릭터 이미지를 동적으로 생성
        for (i in 0 until pullCount) {

            // 1. 캐릭터와 New 뱃지를 겹쳐서 담을 상자(FrameLayout)를 만듭니다.
            val frameLayout = FrameLayout(this)
            val frameParams = LinearLayout.LayoutParams(
                if (pullCount == 1) dpToPx(200) else 0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (pullCount == 1) 0f else 1f
            )
            if (pullCount > 1) {
                frameParams.setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }
            frameLayout.layoutParams = frameParams

            // 2. 캐릭터 희귀도 확률 및 뽑기 로직
            val pool3Star = listOf(2, 10, 17)
            val pool2Star = listOf(4, 6, 8, 14, 16, 18, 20)
            val pool1Star = listOf(1, 3, 5, 7, 9, 11, 12, 13, 15, 19)

            val pickupCharacter = 2
            val dice = (1..100).random()

            val randomCharNum = when {
                dice <= 5 -> {
                    if (summonType == "PICKUP") {
                        if ((1..100).random() <= 50) pickupCharacter
                        else {
                            val remaining3Stars = pool3Star.filter { it != pickupCharacter }
                            if (remaining3Stars.isNotEmpty()) remaining3Stars.random() else pickupCharacter
                        }
                    } else pool3Star.random()
                }
                dice <= 30 -> pool2Star.random()
                else -> pool1Star.random()
            }

            // 신규 획득 여부 검사 및 데이터 저장
            val isNew = !CharacterDataManager.isCharacterAcquired(this, randomCharNum)
            CharacterDataManager.acquireCharacter(this, randomCharNum)

            // 3. 캐릭터 이미지 세팅
            val characterImageView = ImageView(this)
            characterImageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            characterImageView.scaleType = ImageView.ScaleType.FIT_CENTER

            val imageName = "character_${randomCharNum}_gacha"
            val resourceId = resources.getIdentifier(imageName, "drawable", packageName)

            if (resourceId != 0) {
                characterImageView.setImageResource(resourceId)
            } else {
                characterImageView.setImageResource(R.mipmap.ic_launcher)
            }

            // 하얗게 가리기
            characterImageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

            // 상자에 캐릭터 이미지 쏙 넣기
            frameLayout.addView(characterImageView)

            // 4. 신규 캐릭터일 경우에만 New 뱃지 이미지 겹치기
            if (isNew) {
                val badgeImageView = ImageView(this).apply {
                    setImageResource(R.drawable.summon_new) // res/drawable/badge_new.png
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    alpha = 0f // 아직 화면을 안 눌렀으니 투명하게 숨김
                }

                val badgeParams = FrameLayout.LayoutParams(
                    dpToPx(50), // New 뱃지 가로 크기
                    dpToPx(24)  // New 뱃지 세로 크기
                ).apply {
                    gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                    topMargin = dpToPx(50) // 머리 위 여백
                }
                badgeImageView.layoutParams = badgeParams

                // 상자에 New 뱃지 이미지 쏙 넣기
                frameLayout.addView(badgeImageView)

                // 나중에 찾기 위해 꼬리표(tag) 달아두기
                frameLayout.tag = badgeImageView
            }

            // 5. 완성된 상자를 애니메이션과 함께 떨어뜨림
            frameLayout.translationY = -screenHeight
            binding.containerCharacters.addView(frameLayout)

            frameLayout.animate()
                .translationY(0f)
                .setStartDelay(i * 150L)
                .setDuration(600L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        // ==========================================
        // [화면 클릭 이벤트] 정체 공개!
        // ==========================================
        binding.rootLayout.setOnClickListener {
            if (!isRevealed) {
                isRevealed = true
                binding.tvTouchGuide.text = "화면을 터치하여 돌아가기"

                for (i in 0 until binding.containerCharacters.childCount) {
                    // 상자(FrameLayout)를 하나씩 꺼냄
                    val frame = binding.containerCharacters.getChildAt(i) as? FrameLayout

                    // 상자 안의 첫 번째 요소 = 캐릭터 이미지 (하얀 필터 벗기기)
                    val imgView = frame?.getChildAt(0) as? ImageView
                    imgView?.clearColorFilter()

                    // 꼬리표(tag)를 확인해서 New 이미지가 있다면 스르륵 나타나게 함
                    val newBadge = frame?.tag as? ImageView
                    newBadge?.animate()?.alpha(1f)?.setDuration(400L)?.start()
                }
            } else {
                finish()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}