package com.example.gachagame

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class StageActivity : BaseGameActivity() {
    override fun createRootScene(gctx: GameContext): Scene {
        return StageScene(gctx, this)
    }
}
