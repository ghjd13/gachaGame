package com.example.gachagame

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class LoadingActivity : BaseGameActivity() {
    override val drawsDebugInfo = false
    override val drawsFpsGraph = false

    override fun createRootScene(gctx: GameContext): Scene {
        gctx.metrics.setSize(1600f, 900f)

        val destination = intent.getStringExtra("DESTINATION") ?: "MAIN"
        return LoadingScene(gctx, this, destination)
    }
}
