package com.example.gachagame

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class StageScene(
    gctx: GameContext,
    private val activity: Activity,
) : Scene(gctx) {
    enum class Layer { BACKGROUND, UI }
    override val world = World(Layer.values())

    private val stage11 = StageButton(gctx, 700f, 320f, 480f, 120f, "Stage 1-1", "1-1")
    private val stage12 = StageButton(gctx, 600f, 480f, 480f, 120f, "Stage 1-2", "1-2")
    private val stage13 = StageButton(gctx, 500f, 640f, 480f, 120f, "Stage 1-3", "1-3")

    init {
        val screenW = 1600f
        val screenH = 900f
        gctx.metrics.setSize(screenW, screenH)

        val background = object : Sprite(gctx, R.drawable.bg_stage) {
            init {
                setCenter(screenW / 2, screenH / 2)
                setSize(screenW, screenH)
            }
        }

        world.add(background, Layer.BACKGROUND)
        world.add(stage11, Layer.UI)
        world.add(stage12, Layer.UI)
        world.add(stage13, Layer.UI)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawText("Stage Select", 350f, 150f, titlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event)
        }

        val point = gctx.metrics.fromScreen(event.x, event.y)

        if (stage11.isTouched(point.x, point.y)) {

            val intent = Intent(activity, LoadingActivity::class.java).apply {
                putExtra("DESTINATION", "BATTLE")
                putExtra("STAGE_ID", "1-1")
            }
            activity.startActivity(intent)
            return true
        }

        else if (stage12.isTouched(point.x, point.y)) {

            val intent = Intent(activity, LoadingActivity::class.java).apply {
                putExtra("DESTINATION", "BATTLE")
                putExtra("STAGE_ID", "1-2")
            }
            activity.startActivity(intent)
            return true
        }

        else if (stage13.isTouched(point.x, point.y)) {

            val intent = Intent(activity, LoadingActivity::class.java).apply {
                putExtra("DESTINATION", "BATTLE")
                putExtra("STAGE_ID", "1-3")
            }
            activity.startActivity(intent)
            return true
        }

        return super.onTouchEvent(event)
    }

    private class StageButton(
        gctx: GameContext,
        private val cx: Float,
        private val cy: Float,
        private val width: Float,
        private val height: Float,
        private val label: String,
        private val stageId: String,
    ) : IGameObject {
        private val rect = RectF(
            cx - width / 2f,
            cy - height / 2f,
            cx + width / 2f,
            cy + height / 2f,
        )
        // 리소스 매니저를 통해 stage_button 이미지 불러오기
        private val bitmap = gctx.res.getBitmap(R.drawable.stage_button)

        // 잠금/해금 상태에 따라 이미지 투명도를 조절할 페인트 객체
        private val bitmapPaint = Paint().apply { isAntiAlias = true }

        private val textPaint = Paint().apply {
            color = Color.DKGRAY // 밝은 하늘색 버튼에 어울리는 짙은 회색 텍스트
            textSize = 44f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        fun isTouched(x: Float, y: Float): Boolean {
            if (!StageManager.isUnlocked(stageId)) {
                return false
            }
            return rect.contains(x, y)
        }

        override fun update(gctx: GameContext) {
        }

        override fun draw(canvas: Canvas) {
            val unlocked = StageManager.isUnlocked(stageId)

            // 💡 잠긴 스테이지면 이미지 투명도를 낮춰서 비활성화된 느낌을 줍니다.
            bitmapPaint.alpha = if (unlocked) 255 else 100

            // 버튼 이미지 렌더링
            canvas.drawBitmap(bitmap, null, rect, bitmapPaint)

            // 버튼 위에 스테이지 텍스트 렌더링
            val textOffset = (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, cx, cy - textOffset, textPaint)

            // 잠긴 스테이지에 자물쇠 아이콘 대체 텍스트 표시
            if (!unlocked) {
                canvas.drawText("LOCKED", cx, cy + height/2f + 30f, Paint().apply {
                    color = Color.GRAY; textSize = 24f; textAlign = Paint.Align.CENTER
                })
            }
        }
    }
    override fun onEnter() {
        gctx.res.sound.playMusic(R.raw.bgm_stage)
    }
    override fun onExit() {
        gctx.res.sound.stopMusic()
    }
    override fun onPause() {
        gctx.res.sound.pauseMusic()
    }
    override fun onResume() {
        gctx.res.sound.resumeMusic()
    }
    companion object {
        private val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
    }
}
