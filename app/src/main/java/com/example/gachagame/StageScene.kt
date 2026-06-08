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
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class StageScene(
    gctx: GameContext,
    private val activity: Activity,
) : Scene(gctx) {
    enum class Layer { BACKGROUND, UI }
    override val world = World(Layer.values())

    private val stageButton = StageButton(800f, 470f, 420f, 140f, "Stage 1-1")

    init {
        val screenW = 1600f
        val screenH = 900f
        gctx.metrics.setSize(screenW, screenH)

        val background = object : IGameObject {
            private val paint = Paint().apply {
                color = Color.rgb(46, 46, 46)
                style = Paint.Style.FILL
            }

            override fun update(gctx: GameContext) {
            }

            override fun draw(canvas: Canvas) {
                canvas.drawRect(0f, 0f, screenW, screenH, paint)
            }
        }

        world.add(background, Layer.BACKGROUND)
        world.add(stageButton, Layer.UI)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawText("Stage Select", 800f, 210f, titlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event)
        }

        val point = gctx.metrics.fromScreen(event.x, event.y)
        if (stageButton.isTouched(point.x, point.y)) {
            val intent = Intent(activity, LoadingActivity::class.java).apply {
                putExtra("DESTINATION", "BATTLE")
                putExtra("STAGE_ID", "1-1")
            }
            activity.startActivity(intent)
            return true
        }

        return super.onTouchEvent(event)
    }

    private class StageButton(
        private val cx: Float,
        private val cy: Float,
        private val width: Float,
        private val height: Float,
        private val label: String,
    ) : IGameObject {
        private val rect = RectF(
            cx - width / 2f,
            cy - height / 2f,
            cx + width / 2f,
            cy + height / 2f,
        )
        private val fillPaint = Paint().apply {
            color = Color.rgb(52, 152, 219)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        private val strokePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        fun isTouched(x: Float, y: Float): Boolean {
            return rect.contains(x, y)
        }

        override fun update(gctx: GameContext) {
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(rect, 24f, 24f, fillPaint)
            canvas.drawRoundRect(rect, 24f, 24f, strokePaint)

            val textOffset = (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, cx, cy - textOffset, textPaint)
        }
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
