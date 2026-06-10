package com.example.gachagame

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class LoadingScene(
    gctx: GameContext,
    private val activity: Activity,
    private val destination: String
) : Scene(gctx) {

    enum class Layer { BACKGROUND }
    override val world = World(Layer.values())

    // 텍스트를 그릴 Paint 객체 설정
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        textAlign = Paint.Align.RIGHT // 오른쪽 정렬
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    // 흘러간 시간을 기록할 변수
    private var elapsedTime = 0f

    // 화면 이동이 여러 번 발생하는 것을 막기 위한 플래그
    private var isTransitioning = false

    init {
        val screenW = gctx.metrics.width
        val screenH = gctx.metrics.height

        // 1. 랜덤 이미지 목록 구성 및 뽑기
        val loadingImages = arrayOf(
            R.drawable.bg_loading_image1,
            R.drawable.bg_loading_image2,
            R.drawable.bg_loading_image3
        )
        val randomImageId = loadingImages.random()

        // 2. 뽑힌 이미지를 배경 Sprite로 만들어 World에 추가
        val background = object : Sprite(gctx, randomImageId) {
            init {
                setCenter(screenW / 2, screenH / 2)
                setSize(screenW, screenH)
            }
        }
        world.add(background, Layer.BACKGROUND)
    }

    // 엔진에서 매 프레임마다 호출하는 함수 (여기서 타이머 역할을 수행합니다)
    override fun update(gctx: GameContext) {
        super.update(gctx)

        // 이전 프레임에서 현재 프레임까지 걸린 시간(초)을 더합니다.
        elapsedTime += gctx.frameTime

        // 2초(2.0f)가 지났고, 아직 이동 중이 아니라면 다음 액티비티로 넘어갑니다.
        if (elapsedTime >= 2.0f && !isTransitioning) {
            isTransitioning = true // 중복 실행 방지

            when (destination) {
                "SUMMON" -> {
                    val nextIntent = Intent(activity, SummonActivity::class.java)
                    activity.startActivity(nextIntent)
                }
                "STAGE" -> {
                    val nextIntent = Intent(activity, StageActivity::class.java)
                    activity.startActivity(nextIntent)
                }
                "BATTLE" -> {
                    // TODO: 나중에 BattleActivity 만드시면 주석 해제하세요!
                    // val nextIntent = Intent(activity, BattleActivity::class.java)
                    // activity.startActivity(nextIntent)
                    val nextIntent = Intent(activity, BattleActivity::class.java).apply {
                        putExtra(
                            BattleActivity.KEY_STAGE_ID,
                            activity.intent.getStringExtra(BattleActivity.KEY_STAGE_ID)
                        )
                    }
                    activity.startActivity(nextIntent)
                }
            }

            // 로딩 액티비티 종료
            activity.finish()
        }
    }

    // 화면 그리기
    override fun draw(canvas: Canvas) {
        super.draw(canvas) // 배경 이미지를 그립니다.

        // 우측 하단에 Now Loading 텍스트 그리기
        val screenW = gctx.metrics.width
        val screenH = gctx.metrics.height
        canvas.drawText("Now Loading...", screenW - 80f, screenH - 40f, textPaint)
    }
}
