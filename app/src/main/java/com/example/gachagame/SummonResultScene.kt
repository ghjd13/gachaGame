package com.example.gachagame

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class SummonResultScene(
    gctx: GameContext,
    private val activity: Activity,
    private val pullCount: Int,
    private val summonType: String
) : Scene(gctx) {

    enum class Layer { BACKGROUND, CARD, UI }
    override val world = World(Layer.values())

    private var isRevealed = false
    private val cardList = mutableListOf<GachaCard>()

    private val newBadgeBmp: Bitmap = gctx.res.getBitmap(R.drawable.summon_new)

    private val guideTextPaint = Paint().apply {
        color = Color.WHITE; textSize = 40f
        textAlign = Paint.Align.CENTER; isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    init {
        val screenW = 1600f
        val screenH = 900f

        val bg = object : Sprite(gctx, R.drawable.summon_result_bg) {
            init { setCenter(screenW / 2, screenH / 2); setSize(screenW, screenH) }
        }
        world.add(bg, Layer.BACKGROUND)

        val pool3Star = listOf(2, 10, 17)
        val pool2Star = listOf(4, 6, 8, 14, 16, 18, 20)
        val pool1Star = listOf(1, 3, 5, 7, 9, 11, 12, 13, 15, 19)
        val pickupCharacter = 2

        val cardW = 145f
        val spacing = 10f
        val totalWidth = (cardW * pullCount) + (spacing * (pullCount - 1))
        val startX = (screenW - totalWidth) / 2f

        for (i in 0 until pullCount) {
            val dice = (1..100).random()
            val randomCharNum = when {
                dice <= 5 -> {
                    if (summonType == "PICKUP") {
                        if ((1..100).random() <= 50) pickupCharacter else (pool3Star.filter { it != pickupCharacter }.randomOrNull() ?: pickupCharacter)
                    } else pool3Star.random()
                }
                dice <= 30 -> pool2Star.random()
                else -> pool1Star.random()
            }

            // 올려주신 CharacterDataManager를 사용하여 도감 확인 및 획득 처리
            val isNew = !CharacterDataManager.isCharacterAcquired(activity, randomCharNum)
            CharacterDataManager.acquireCharacter(activity, randomCharNum)

            android.util.Log.d("GachaTest", "뽑은 캐릭터 ID: $randomCharNum / NEW 뱃지 여부(isNew): $isNew")

            val targetX = startX + (cardW / 2f) + i * (cardW + spacing)
            val dropDelay = i * 0.15f

            var resId = activity.resources.getIdentifier("character_${randomCharNum}_gacha", "drawable", activity.packageName)
            if (resId == 0) resId = R.drawable.character_10_gacha

            val card = GachaCard(gctx, isNew, targetX, dropDelay, resId, cardW)
            cardList.add(card)
            world.add(card, Layer.CARD)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val msg = if (!isRevealed) "화면을 터치하여 확인하세요" else "화면을 터치하여 돌아가기"
        canvas.drawText(msg, 1600f / 2, 850f, guideTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val allLanded = cardList.all { it.hasLanded }
            if (!allLanded) return true

            if (!isRevealed) {
                isRevealed = true
                gctx.res.sound.playEffect(R.raw.sfx_summon_result)
                cardList.forEach { it.reveal() }
            } else {
                activity.finish()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    inner class GachaCard(
        gctx: GameContext, val isNew: Boolean,
        val targetX: Float, delay: Float,
        resId: Int, val cardW: Float
    ) : Sprite(gctx, resId) {

        var waitTimer = delay
        var hasLanded = false
        var revealed = false

        private val silhouettePaint = Paint().apply {
            colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
        private val badgeRect = RectF()
        private val cardH = cardW * (bitmapHeight.toFloat() / bitmapWidth.toFloat())
        private val targetY = (cardH / 2f) - 60f
        private val startY = targetY - 1000f
        private var animTime = 0f
        private val duration = 0.6f

        init {
            setCenter(targetX, startY)
            setSize(cardW, cardH)
        }

        fun reveal() { revealed = true }

        override fun update(gctx: GameContext) {
            if (waitTimer > 0) {
                waitTimer -= gctx.frameTime
                return
            }

            if (!hasLanded) {
                animTime += gctx.frameTime
                val t = (animTime / duration).coerceAtMost(1f)
                val progress = 1f - (1f - t) * (1f - t)

                val currentY = startY + (targetY - startY) * progress
                setCenter(targetX, currentY)
                setSize(cardW, cardH)

                if (t >= 1f) {
                    hasLanded = true
                }
            }
        }

        override fun draw(canvas: Canvas) {
            if (waitTimer > 0) return

            if (!revealed) {
                canvas.drawBitmap(bitmap, srcRect, dstRect, silhouettePaint)
            } else {
                super.draw(canvas)

                if (!isNew) {
                    val bw = 80f
                    val bh = 40f
                    val badgeTop = dstRect.top - 80f
                    badgeRect.set(targetX - bw/2, badgeTop, targetX + bw/2, badgeTop + bh)
                    canvas.drawBitmap(newBadgeBmp, null, badgeRect, null)
                }
            }
        }
    }
    // 💡 [추가] 결과창 씬 전용 BGM 생명주기 관리
    override fun onEnter() {
        // 원하시는 결과창 브금 파일명으로 변경하세요
        gctx.res.sound.playMusic(R.raw.bgm_summon_result)
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
}