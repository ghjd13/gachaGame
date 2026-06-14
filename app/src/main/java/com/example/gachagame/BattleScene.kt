package com.example.gachagame

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
import kr.ac.tukorea.ge.spgp2026.a2dg.util.Gauge // [추가] Gauge 클래스 임포트
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class BattleScene(
    gctx: GameContext,
    private val stageId: String,
) : Scene(gctx) {
    override val clipsRect = true
    enum class Layer { BACKGROUND, PLAYER_BULLET, ENEMY, ENEMY_BULLET, PLAYER, UI }

    override val world = World(Layer.values())

    private val player = PlayerShip()
    private val hud = BattleHud()
    private var enemySpawnTimer = 0f
    private var playerFireTimer = 0f

    private var spawnedEnemyCount = 0
    private val maxEnemies: Int

    init {
        val screenW = 1600f
        val screenH = 900f
        gctx.metrics.setSize(screenW, screenH)

        maxEnemies = when (stageId) {
            "1-1" -> 10 // 1-1 스테이지는 10마리
            "1-2" -> 20 // 1-2 스테이지는 20마리
            "2-1" -> 30 // 2-1 스테이지는 30마리
            else -> 10  // 그 외 기본값
        }

        // 1. 원경 (하늘) - 천천히 이동 (예: 속도 30f)
        val skyBackground = kr.ac.tukorea.ge.spgp2026.a2dg.objects.HorzScrollBackground(
            gctx,
            R.drawable.bg_sky, // 투명 처리된 하늘 이미지
            -30f
        )

        // 2. 근경 (땅/바다) - 빠르게 이동 (예: 하늘보다 빠른 120f)
        val groundBackground = kr.ac.tukorea.ge.spgp2026.a2dg.objects.HorzScrollBackground(
            gctx,
            R.drawable.bg_ground, // 투명 처리된 땅 이미지
            -120f
        )

        world.add(skyBackground, Layer.BACKGROUND)
        world.add(groundBackground, Layer.BACKGROUND)

        world.add(player, Layer.PLAYER)
        world.add(hud, Layer.UI)
    }

    override fun update(gctx: GameContext) {
        enemySpawnTimer += gctx.frameTime
        playerFireTimer += gctx.frameTime

        if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL && spawnedEnemyCount < maxEnemies) {
            enemySpawnTimer = 0f
            spawnEnemy()
            spawnedEnemyCount++
        }

        if (playerFireTimer >= PLAYER_FIRE_INTERVAL) {
            playerFireTimer = 0f
            firePlayerBullet()
        }

        super.update(gctx)
        checkCollisions()

        if (spawnedEnemyCount >= maxEnemies && world.objectsAt(Layer.ENEMY).isEmpty()) {
            gctx.sceneStack.pop()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val point = gctx.metrics.fromScreen(event.x, event.y)
                player.moveTo(point.y)
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> true
            else -> super.onTouchEvent(event)
        }
    }

    private fun spawnEnemy() {
        val y = Random.nextFloat() * (SCREEN_H - ENEMY_MARGIN * 2f) + ENEMY_MARGIN
        val enemy = EnemyShip(SCREEN_W + 80f, y)
        world.add(enemy, Layer.ENEMY)
    }

    private fun firePlayerBullet() {
        if (!player.isAlive) return

        val bullet = Bullet(
            x = player.x + 58f,
            y = player.y,
            speed = PLAYER_BULLET_SPEED,
            damage = player.attackPower,
            fromPlayer = true,
        )
        world.add(bullet, Layer.PLAYER_BULLET)
    }

    private fun fireEnemyBullet(enemy: EnemyShip) {
        val bullet = Bullet(
            x = enemy.x - 58f,
            y = enemy.y,
            speed = ENEMY_BULLET_SPEED,
            damage = enemy.attackPower,
            fromPlayer = false,
        )
        world.add(bullet, Layer.ENEMY_BULLET)
    }

    private fun checkCollisions() {
        val playerBullets = world.objectsAt(Layer.PLAYER_BULLET).filterIsInstance<Bullet>()
        val enemies = world.objectsAt(Layer.ENEMY).filterIsInstance<EnemyShip>()

        for (bullet in playerBullets) {
            if (!bullet.isActive) continue
            for (enemy in enemies) {
                if (!enemy.isAlive) continue
                if (RectF.intersects(bullet.rect, enemy.rect)) {
                    bullet.isActive = false
                    enemy.takeDamage(bullet.damage)
                    break
                }
            }
        }

        val enemyBullets = world.objectsAt(Layer.ENEMY_BULLET).filterIsInstance<Bullet>()
        for (bullet in enemyBullets) {
            if (!bullet.isActive || !player.isAlive) continue
            if (RectF.intersects(bullet.rect, player.hitRect)) {
                bullet.isActive = false
                player.tryTakeDamage(bullet.damage)
            }
        }

        removeInactiveObjects()
    }

    private fun removeInactiveObjects() {
        world.objectsAt(Layer.PLAYER_BULLET)
            .filterIsInstance<Bullet>()
            .filter { !it.isActive || it.x > SCREEN_W + 120f || it.y < -120f || it.y > SCREEN_H + 120f }
            .forEach { world.remove(it, Layer.PLAYER_BULLET) }

        world.objectsAt(Layer.ENEMY_BULLET)
            .filterIsInstance<Bullet>()
            .filter { !it.isActive || it.x < -120f || it.y < -120f || it.y > SCREEN_H + 120f }
            .forEach { world.remove(it, Layer.ENEMY_BULLET) }

        world.objectsAt(Layer.ENEMY)
            .filterIsInstance<EnemyShip>()
            .filter { !it.isAlive }
            .forEach { world.remove(it, Layer.ENEMY) }
    }

    private fun nearestEnemyTo(x: Float, y: Float): EnemyShip? {
        var nearest: EnemyShip? = null
        var nearestDistanceSq = Float.MAX_VALUE

        for (enemy in world.objectsAt(Layer.ENEMY).filterIsInstance<EnemyShip>()) {
            if (!enemy.isAlive) continue

            val dx = enemy.x - x
            val dy = enemy.y - y
            val distanceSq = dx * dx + dy * dy
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq
                nearest = enemy
            }
        }

        return nearest
    }

    private inner class PlayerShip : IGameObject {
        var x = PLAYER_X
            private set
        var y = SCREEN_H / 2f
            private set
        var hp = PLAYER_MAX_HP
            private set
        val attackPower = 18
        val evasionRate = 0.25f
        val rect = RectF()
        val hitRect = RectF()
        val isAlive: Boolean
            get() = hp > 0

        private val sprite = kr.ac.tukorea.ge.spgp2026.a2dg.objects.AnimSprite(
            gctx = gctx,
            resId = R.drawable.character_1_sd_move_sheet, // 3장을 가로로 합친 이미지
            fps = 8f,      // 초당 8프레임 속도로 발 구르기
            frameCount = 3 // 3칸으로 나뉘어 있는 시트임을 엔진에 알림
        )

        init {
            sprite.setSize(76f, 108f)
            syncRect()
        }

        fun moveTo(targetY: Float) {
            y = targetY.coerceIn(PLAYER_HALF_H, SCREEN_H - PLAYER_HALF_H)
            syncRect()
        }

        fun tryTakeDamage(damage: Int) {
            if (Random.nextFloat() < evasionRate) return
            hp = max(0, hp - damage)
        }

        override fun update(gctx: GameContext) {
            sprite.update(gctx)
        }

        override fun draw(canvas: Canvas) {
            sprite.draw(canvas)
        }

        private fun syncRect() {
            rect.set(x - PLAYER_HALF_W, y - PLAYER_HALF_H, x + PLAYER_HALF_W, y + PLAYER_HALF_H)
            hitRect.set(x - PLAYER_HIT_HALF_W, y - PLAYER_HIT_HALF_H, x + PLAYER_HIT_HALF_W, y + PLAYER_HIT_HALF_H)

            sprite.x = x
            sprite.y = y
            sprite.syncDstRect()
        }
    }

    private inner class EnemyShip(
        startX: Float,
        startY: Float,
    ) : IGameObject {
        var x = startX
            private set
        var y = startY
            private set
        var hp = ENEMY_MAX_HP
            private set
        val attackPower = 10
        val rect = RectF()
        val isAlive: Boolean
            get() = hp > 0

        private var fireTimer = Random.nextFloat() * ENEMY_FIRE_INTERVAL
        private val sprite = kr.ac.tukorea.ge.spgp2026.a2dg.objects.AnimSprite(
            gctx = gctx,
            resId = R.drawable.enemy1_move_sheet,
            fps = 8f,      // 애니메이션 속도
            frameCount = 3 // 시트 칸 수 (3칸이면 3)
        )

        init {
            sprite.setSize(116f, 88f)
            syncRect()
        }

        fun takeDamage(damage: Int) {
            hp = max(0, hp - damage)
        }

        override fun update(gctx: GameContext) {
            if (x > ENEMY_STOP_X) {
                x = max(ENEMY_STOP_X, x - ENEMY_MOVE_SPEED * gctx.frameTime)
            }

            fireTimer += gctx.frameTime
            if (fireTimer >= ENEMY_FIRE_INTERVAL) {
                fireTimer = 0f
                fireEnemyBullet(this)
            }

            sprite.update(gctx)
            syncRect()
        }

        override fun draw(canvas: Canvas) {
            sprite.draw(canvas)
        }

        private fun syncRect() {
            rect.set(x - ENEMY_HALF_W, y - ENEMY_HALF_H, x + ENEMY_HALF_W, y + ENEMY_HALF_H)

            sprite.x = x
            sprite.y = y
            sprite.syncDstRect() //
        }
    }

    private inner class Bullet(
        var x: Float,
        var y: Float,
        private val speed: Float,
        val damage: Int,
        private val fromPlayer: Boolean,
    ) : IGameObject {
        var isActive = true
        val rect = RectF()
        private var vx = 0f
        private var vy = 0f
        private var angleDegrees = 0f
        private val paint = Paint().apply {
            color = if (fromPlayer) Color.rgb(125, 245, 255) else Color.rgb(255, 210, 80)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        init {
            aimAtCurrentTarget()
            syncRect()
        }

        override fun update(gctx: GameContext) {
            // 플레이어 탄일 때만 매 프레임 타겟을 다시 조준하여 유도탄처럼 동작하게 합니다.
            if (fromPlayer) {
                aimAtCurrentTarget()
            }
            angleDegrees = Math.toDegrees(atan2(vy.toDouble(), vx.toDouble())).toFloat()

            x += vx * gctx.frameTime
            y += vy * gctx.frameTime
            syncRect()
        }

        override fun draw(canvas: Canvas) {
            canvas.save()

            canvas.rotate(angleDegrees, x, y)

            if (fromPlayer) {
                canvas.drawRoundRect(rect, 14f, 14f, paint)
            } else {
                canvas.drawOval(rect, paint)
            }

            canvas.restore()
        }

        private fun syncRect() {
            rect.set(x - BULLET_HALF_W, y - BULLET_HALF_H, x + BULLET_HALF_W, y + BULLET_HALF_H)
        }

        private fun aimAtCurrentTarget() {
            val enemyTarget = if (fromPlayer) nearestEnemyTo(x, y) else null
            if (fromPlayer && enemyTarget == null) {
                vx = speed
                vy = 0f
                return
            }
            if (!fromPlayer && !player.isAlive) {
                vx = speed
                vy = 0f
                return
            }

            val targetX = enemyTarget?.x ?: player.x
            val targetY = enemyTarget?.y ?: player.y
            val dx = targetX - x
            val dy = targetY - y
            val distance = sqrt(dx * dx + dy * dy)
            if (distance <= 0.001f) return

            val angle = atan2(dy, dx)
            val absoluteSpeed = kotlin.math.abs(speed)
            vx = cos(angle) * absoluteSpeed
            vy = sin(angle) * absoluteSpeed
        }
    }

    private inner class BattleHud : IGameObject {
        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // [수정] 업로드해주신 Gauge 클래스를 사용하여 체력바 객체 생성
        // 오류의 원인이었던 thickness를 '가로 길이(320) 대비 비율'로 정확하게 설정했습니다.
        private val hpGauge = Gauge(
            thickness = 28f / 320f,
            fgColor = Color.rgb(80, 235, 130),
            bgColor = Color.argb(180, 40, 40, 40)
        )

        override fun update(gctx: GameContext) {
        }

        override fun draw(canvas: Canvas) {
            canvas.drawText("Stage $stageId", 32f, 52f, textPaint)
            canvas.drawText("ATK ${player.attackPower}  EVA ${(player.evasionRate * 100f).toInt()}%", 32f, 94f, textPaint)

            val hpRatio = if (PLAYER_MAX_HP == 0) 0f else player.hp / PLAYER_MAX_HP.toFloat()

            // [수정] 직접 사각형을 2개 그리는 대신 hpGauge의 draw 함수를 호출
            // x: 시작점(32), y: 중앙선 높이(118 + 14 = 132), scale: 가로길이(320)
            hpGauge.draw(canvas, 32f, 132f, 320f, hpRatio)

            canvas.drawText("HP ${player.hp}/$PLAYER_MAX_HP", 370f, 145f, textPaint)
        }
    }

    companion object {
        private const val SCREEN_W = 1600f
        private const val SCREEN_H = 900f
        private const val PLAYER_X = 210f
        private const val PLAYER_HALF_W = 38f
        private const val PLAYER_HALF_H = 54f
        private const val PLAYER_HIT_HALF_W = 22f
        private const val PLAYER_HIT_HALF_H = 22f
        private const val PLAYER_MAX_HP = 120
        private const val PLAYER_FIRE_INTERVAL = 0.42f
        private const val PLAYER_BULLET_SPEED = 760f

        private const val ENEMY_STOP_X = SCREEN_W / 3f
        private const val ENEMY_MARGIN = 110f
        private const val ENEMY_HALF_W = 58f
        private const val ENEMY_HALF_H = 44f
        private const val ENEMY_MAX_HP = 60
        private const val ENEMY_SPAWN_INTERVAL = 1.5f
        private const val ENEMY_FIRE_INTERVAL = 1.25f
        private const val ENEMY_MOVE_SPEED = 260f
        private const val ENEMY_BULLET_SPEED = -520f

        private const val BULLET_HALF_W = 20f
        private const val BULLET_HALF_H = 8f
    }
}