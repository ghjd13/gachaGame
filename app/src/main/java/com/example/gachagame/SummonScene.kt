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

class SummonScene(gctx: GameContext, private val activity: Activity) : Scene(gctx) {

    enum class Layer { BACKGROUND, EFFECT, UI }
    override val world = World(Layer.values())

    private var currentSummonType = "PICKUP"

    private lateinit var tabPickup: TabSprite
    private lateinit var tabNormal: TabSprite
    private lateinit var banner: BannerSprite

    private lateinit var btnSummon1: CanvasButton
    private lateinit var btnSummon10: CanvasButton
    private lateinit var btnBack: BackButton

    init {
        val screenW = 1600f
        val screenH = 900f

        // 1. 배경 이미지
        val bg = object : Sprite(gctx, R.drawable.summon_bg) {
            init { setCenter(screenW / 2, screenH / 2); setSize(screenW, screenH) }
        }
        world.add(bg, Layer.BACKGROUND)

        // 2. 좌측 띠 (XML의 view_left_stripe 대체 - 파란색 반투명 사각형)
        val leftStripe = object : IGameObject {
            val paint = Paint().apply { color = Color.parseColor("#4087CEFA") }
            override fun update(gctx: GameContext) {}
            override fun draw(canvas: Canvas) {
                canvas.drawRect(80f, 0f, 160f, screenH, paint)
            }
        }
        world.add(leftStripe, Layer.EFFECT)

        // 3. 배너 이미지
        banner = BannerSprite(gctx, R.drawable.summon_banner_pickup).apply {
            setup(1000f, 450f, 850f, 480f) // 중앙 약간 우측에 크게 배치
        }
        world.add(banner, Layer.UI)

        // 4. 좌측 탭 (클릭 시 튀어나오는 연출)
        tabPickup = TabSprite(gctx, R.drawable.summon_btn_pickup, "PICKUP").apply {
            baseX = 260f
            baseY = 300f
        }
        world.add(tabPickup, Layer.UI)

        tabNormal = TabSprite(gctx, R.drawable.summon_btn_normal, "NORMAL").apply {
            baseX = 260f
            baseY = 460f
        }
        world.add(tabNormal, Layer.UI)

        // 5. 버튼들 (XML 도형 에러 방지용 Canvas 객체들)
        btnBack = BackButton(120f, 100f) // 뒤로가기 동그라미 버튼

        btnSummon1 = CanvasButton(1150f, 750f, 240f, 120f, "1회 소환", "#3498DB")
        btnSummon10 = CanvasButton(1420f, 750f, 280f, 120f, "10회 소환", "#E74C3C")

        world.add(btnBack, Layer.UI)
        world.add(btnSummon1, Layer.UI)
        world.add(btnSummon10, Layer.UI)

        updateTabs()
    }

    private fun updateTabs() {
        // 선택된 탭은 오른쪽으로 20픽셀 튀어나오게 위치 설정 (XML의 marginStart 조절 효과)
        tabPickup.isSelected = (currentSummonType == "PICKUP")
        tabNormal.isSelected = (currentSummonType == "NORMAL")
        tabPickup.syncPosition()
        tabNormal.syncPosition()

        val newRes = if (currentSummonType == "PICKUP") R.drawable.summon_banner_pickup else R.drawable.summon_banner_normal
        banner.changeImage(newRes)
    }

    // 1. 터치 잠금용 변수 추가
    private var isTransitioning = false

    // SummonScene.kt 내부
    override fun onEnter() { gctx.res.sound.playMusic(R.raw.bgm_summon) }
    override fun onExit() { gctx.res.sound.stopMusic() }
    override fun onPause() { gctx.res.sound.pauseMusic() }

    override fun onResume() {
        super.onResume()
        isTransitioning = false
        gctx.res.sound.resumeMusic() // 👈 여기에 추가
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTransitioning) return true

        val pos = gctx.metrics.fromScreen(event.x, event.y)

        if (event.action == MotionEvent.ACTION_DOWN) {
            if (btnBack.isTouched(pos.x, pos.y)) {
                activity.finish()
                return true
            }
            if (tabPickup.isTouched(pos.x, pos.y)) {
                currentSummonType = "PICKUP"
                updateTabs()
                return true
            }
            if (tabNormal.isTouched(pos.x, pos.y)) {
                currentSummonType = "NORMAL"
                updateTabs()
                return true
            }
            if (btnSummon1.isTouched(pos.x, pos.y)) {
                isTransitioning = true
                startGacha(1)
                return true
            }
            if (btnSummon10.isTouched(pos.x, pos.y)) {
                isTransitioning = true
                startGacha(10)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startGacha(count: Int) {
        val intent = Intent(activity, SummonResultActivity::class.java)
        intent.putExtra("PULL_COUNT", count)
        intent.putExtra("SUMMON_TYPE", currentSummonType)
        activity.startActivity(intent)
    }

    // ==========================================
    // 내부 오브젝트 클래스들
    // ==========================================
    inner class BannerSprite(gctx: GameContext, resId: Int) : Sprite(gctx, resId) {
        fun setup(cx: Float, cy: Float, w: Float, h: Float) { setCenter(cx, cy); setSize(w, h) }
        fun changeImage(newResId: Int) { bitmap = gctx.res.getBitmap(newResId) }
    }

    inner class TabSprite(gctx: GameContext, resId: Int, val type: String) : Sprite(gctx, resId) {
        var isSelected = false
        var baseX = 0f
        var baseY = 0f
        fun syncPosition() {
            val cx = if (isSelected) baseX + 20f else baseX
            setCenter(cx, baseY)
            setSize(300f, 150f)
        }
        fun isTouched(tx: Float, ty: Float) = dstRect.contains(tx, ty)
    }

    // 뒤로가기 버튼 (원형)
    inner class BackButton(var cx: Float, var cy: Float) : IGameObject {
        private val radius = 50f
        private val bgPaint = Paint().apply { color = Color.parseColor("#B3FFFFFF"); isAntiAlias = true }
        private val strokePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
        private val textPaint = Paint().apply { color = Color.DKGRAY; textSize = 60f; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }

        fun isTouched(tx: Float, ty: Float) = Math.pow((tx - cx).toDouble(), 2.0) + Math.pow((ty - cy).toDouble(), 2.0) <= radius * radius
        override fun update(gctx: GameContext) {}
        override fun draw(canvas: Canvas) {
            canvas.drawCircle(cx, cy, radius, bgPaint)
            canvas.drawCircle(cx, cy, radius, strokePaint)
            val offset = (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText("◀", cx - 5f, cy - offset, textPaint)
        }
    }

    // 소환 실행 버튼 (둥근 사각형)
    inner class CanvasButton(var cx: Float, var cy: Float, var w: Float, var h: Float, val text: String, colorHex: String) : IGameObject {
        private val rect = RectF(cx - w/2, cy - h/2, cx + w/2, cy + h/2)
        private val bgPaint = Paint().apply { color = Color.parseColor(colorHex); isAntiAlias = true }
        private val textPaint = Paint().apply { color = Color.WHITE; textSize = 40f; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }

        fun isTouched(tx: Float, ty: Float) = rect.contains(tx, ty)
        override fun update(gctx: GameContext) {}
        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(rect, 20f, 20f, bgPaint)
            val offset = (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(text, cx, cy - offset, textPaint)
        }
    }
}