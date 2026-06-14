package com.example.gachagame

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
// [수정됨] a2dg 엔진의 util 패키지에서 매니저를 가져옵니다!
import kr.ac.tukorea.ge.spgp2026.a2dg.util.UserDataManager

class LobbyScene(gctx: GameContext, private val context: Context) : Scene(gctx) {

    enum class Layer { BACKGROUND, CHARACTER, UI }
    override val world = World(Layer.values())

    private val btnBattle: ButtonSprite
    private val btnFormation: ButtonSprite
    private val btnShop: ButtonSprite
    private val btnSummon: ButtonSprite

    private val profileCx = 150f
    private val profileCy = 150f

    // 동적으로 표시할 유저 정보 변수
    private var userNickname = ""
    private var userLevel = 1
    private var userUid = ""
    private var characterResId = R.drawable.character_1_ld

    private val nicknamePaint = Paint().apply { color = Color.WHITE; textSize = 32f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    private val uidLabelPaint = Paint().apply { color = Color.WHITE; textSize = 24f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    private val levelNumPaint = Paint().apply { color = Color.WHITE; textSize = 48f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    private val levelTextPaint = Paint().apply { color = Color.WHITE; textSize = 20f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    private val linePaint = Paint().apply { color = Color.WHITE; strokeWidth = 2f; isAntiAlias = true }

    init {
        // 엔진(a2dg)의 매니저를 통해 로컬 DB를 읽어옵니다.
        loadUserData()

        val screenW = 1600f
        val screenH = 900f

        val background = object : Sprite(gctx, R.drawable.lobby_bg) {
            init { setCenter(screenW / 2, screenH / 2); setSize(screenW, screenH) }
        }
        world.add(background, Layer.BACKGROUND)

        val character = object : Sprite(gctx, characterResId) {
            init { setCenterProportionalHeight(400f, screenH * 0.6f, 800f) }
        }
        world.add(character, Layer.CHARACTER)

        val circleBorder = object : IGameObject {
            val strokePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f; isAntiAlias = true }
            val fillPaint = Paint().apply { color = Color.parseColor("#33424242"); style = Paint.Style.FILL; isAntiAlias = true }
            override fun update(gctx: GameContext) {}
            override fun draw(canvas: Canvas) {
                canvas.drawCircle(profileCx, profileCy, 55f, fillPaint)
                canvas.drawCircle(profileCx, profileCy, 55f, strokePaint)
            }
        }
        world.add(circleBorder, Layer.UI)

        btnBattle = ButtonSprite(gctx, R.drawable.lobby_btn_battle).apply { setup(1080f, 350f, 280f, 180f) }
        btnFormation = ButtonSprite(gctx, R.drawable.lobby_btn_formation).apply { setup(1300f, 350f, 120f, 140f) }
        btnShop = ButtonSprite(gctx, R.drawable.lobby_btn_store).apply { setup(1000f, 550f, 120f, 150f) }
        btnSummon = ButtonSprite(gctx, R.drawable.lobby_btn_summon).apply { setup(1220f, 550f, 280f, 180f) }

        world.add(btnBattle, Layer.UI)
        world.add(btnFormation, Layer.UI)
        world.add(btnShop, Layer.UI)
        world.add(btnSummon, Layer.UI)
    }

    private fun loadUserData() {
        // a2dg 엔진단에 있는 매니저 호출
        val profile = UserDataManager.loadUserData(context)

        userNickname = profile.nickname
        userLevel = profile.level
        userUid = profile.uid

        val resId = context.resources.getIdentifier(profile.characterResName, "drawable", context.packageName)
        if (resId != 0) {
            characterResId = resId
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawText(userLevel.toString(), profileCx, profileCy + 5f, levelNumPaint)
        canvas.drawText("LV", profileCx, profileCy + 40f, levelTextPaint)

        val textStartX = profileCx + 80f
        val textY = profileCy - 20f

        canvas.drawText(userNickname, textStartX, textY, nicknamePaint)

        val lineY = textY + 20f
        canvas.drawLine(textStartX, lineY, textStartX + 250f, lineY, linePaint)
        canvas.drawText("UID: $userUid", textStartX, lineY + 40f, uidLabelPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pos = gctx.metrics.fromScreen(event.x, event.y)

        if (event.action == MotionEvent.ACTION_DOWN) {
            if (btnSummon.isTouched(pos.x, pos.y)) {
                context.startActivity(Intent(context, LoadingActivity::class.java).apply {
                    putExtra("DESTINATION", "SUMMON")
                })
                return true
            }
            if (btnBattle.isTouched(pos.x, pos.y)) {
                context.startActivity(Intent(context, LoadingActivity::class.java).apply {
                    putExtra("DESTINATION", "STAGE")
                })
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    inner class ButtonSprite(gctx: GameContext, resId: Int) : Sprite(gctx, resId) {
        fun setup(cx: Float, cy: Float, w: Float, h: Float) { setCenter(cx, cy); setSize(w, h) }
        fun isTouched(tx: Float, ty: Float) = dstRect.contains(tx, ty)
    }
    override fun onEnter() {
        gctx.res.sound.playMusic(R.raw.bgm_lobby)
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
