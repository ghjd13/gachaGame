package com.example.gachagame

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class SummonActivity : BaseGameActivity() {

    override val drawsDebugInfo = false
    override val drawsFpsGraph = false

    override fun createRootScene(gctx: GameContext): Scene {
        // [필수] 가로 화면 고정 (1600 x 900)
        gctx.metrics.setSize(1600f, 900f)

        return SummonScene(gctx, this)
    }
}