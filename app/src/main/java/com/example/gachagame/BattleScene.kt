package com.example.gachagame

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
    enum class Layer { BACKGROUND, PLAYER_BULLET, ENEMY, ENEMY_BULLET, PLAYER, UI }

    override val world = World(Layer.values())

    private val player = PlayerShip()
    private val hud = BattleHud()
    private var enemySpawnTimer = 0f
    private var playerFireTimer = 0f

    init {
        world.add(BattleBackground(), Layer.BACKGROUND)
        world.add(player, Layer.PLAYER)
        world.add(hud, Layer.UI)
    }

    override fun update(gctx: GameContext) {
        enemySpawnTimer += gctx.frameTime
        playerFireTimer += gctx.frameTime

        if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL) {
            enemySpawnTimer = 0f
            spawnEnemy()
        }

        if (playerFireTimer >= PLAYER_FIRE_INTERVAL) {
            playerFireTimer = 0f
            firePlayerBullet()
        }

        super.update(gctx)
        checkCollisions()
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

    private inner class BattleBackground : IGameObject {
        private var offset = 0f
        private val bgPaint = Paint().apply {
            color = Color.rgb(15, 32, 56)
            style = Paint.Style.FILL
        }
        private val seaPaint = Paint().apply {
            color = Color.rgb(24, 92, 128)
            style = Paint.Style.FILL
        }
        private val linePaint = Paint().apply {
            color = Color.argb(90, 255, 255, 255)
            strokeWidth = 3f
        }
        private val stopLinePaint = Paint().apply {
            color = Color.argb(120, 255, 210, 80)
            strokeWidth = 4f
        }

        override fun update(gctx: GameContext) {
            offset = (offset + 90f * gctx.frameTime) % 160f
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRect(0f, 0f, SCREEN_W, SCREEN_H, bgPaint)
            canvas.drawRect(0f, SCREEN_H * 0.62f, SCREEN_W, SCREEN_H, seaPaint)

            var x = -offset
            while (x < SCREEN_W) {
                canvas.drawLine(x, SCREEN_H * 0.66f, x + 110f, SCREEN_H, linePaint)
                x += 160f
            }

            canvas.drawLine(ENEMY_STOP_X, 0f, ENEMY_STOP_X, SCREEN_H, stopLinePaint)
        }
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

        private val bodyPaint = Paint().apply {
            color = Color.rgb(90, 200, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        private val outlinePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

        init {
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
        }

        override fun draw(canvas: Canvas) {
            canvas.drawOval(rect, bodyPaint)
            canvas.drawOval(rect, outlinePaint)
        }

        private fun syncRect() {
            rect.set(x - PLAYER_HALF_W, y - PLAYER_HALF_H, x + PLAYER_HALF_W, y + PLAYER_HALF_H)
            hitRect.set(x - PLAYER_HIT_HALF_W, y - PLAYER_HIT_HALF_H, x + PLAYER_HIT_HALF_W, y + PLAYER_HIT_HALF_H)
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
        private val bodyPaint = Paint().apply {
            color = Color.rgb(255, 105, 105)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        private val outlinePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

        init {
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

            syncRect()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(rect, 24f, 24f, bodyPaint)
            canvas.drawRoundRect(rect, 24f, 24f, outlinePaint)
        }

        private fun syncRect() {
            rect.set(x - ENEMY_HALF_W, y - ENEMY_HALF_H, x + ENEMY_HALF_W, y + ENEMY_HALF_H)
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
            aimAtCurrentTarget()
            x += vx * gctx.frameTime
            y += vy * gctx.frameTime
            syncRect()
        }

        override fun draw(canvas: Canvas) {
            if (fromPlayer) {
                canvas.drawRoundRect(rect, 14f, 14f, paint)
            } else {
                canvas.drawOval(rect, paint)
            }
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
        private val hpBgPaint = Paint().apply {
            color = Color.argb(180, 40, 40, 40)
            style = Paint.Style.FILL
        }
        private val hpPaint = Paint().apply {
            color = Color.rgb(80, 235, 130)
            style = Paint.Style.FILL
        }

        override fun update(gctx: GameContext) {
        }

        override fun draw(canvas: Canvas) {
            canvas.drawText("Stage $stageId", 32f, 52f, textPaint)
            canvas.drawText("ATK ${player.attackPower}  EVA ${(player.evasionRate * 100f).toInt()}%", 32f, 94f, textPaint)

            val hpRatio = if (PLAYER_MAX_HP == 0) 0f else player.hp / PLAYER_MAX_HP.toFloat()
            canvas.drawRect(32f, 118f, 352f, 146f, hpBgPaint)
            canvas.drawRect(32f, 118f, 32f + 320f * hpRatio, 146f, hpPaint)
            canvas.drawText("HP ${player.hp}/$PLAYER_MAX_HP", 370f, 145f, textPaint)
        }
    }

    companion object {
        private const val SCREEN_W = 1600f
        private const val SCREEN_H = 900f
        private const val PLAYER_X = 210f
        private const val PLAYER_HALF_W = 54f
        private const val PLAYER_HALF_H = 38f
        private const val PLAYER_HIT_HALF_W = 30f
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
