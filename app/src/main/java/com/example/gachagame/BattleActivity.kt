package com.example.gachagame

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BattleActivity : BaseGameActivity() {
    override val drawsDebugInfo = false
    override val drawsFpsGraph = false

    override fun createRootScene(gctx: GameContext): Scene {
        gctx.metrics.setSize(1600f, 900f)
        val stageId = intent.getStringExtra(KEY_STAGE_ID) ?: DEFAULT_STAGE_ID
        return BattleScene(gctx, stageId)
    }

    companion object {
        const val KEY_STAGE_ID = "STAGE_ID"
        private const val DEFAULT_STAGE_ID = "1-1"
    }
}
