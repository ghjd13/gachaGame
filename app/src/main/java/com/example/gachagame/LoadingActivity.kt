package com.example.gachagame

import android.os.Bundle
import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class LoadingActivity : BaseGameActivity() {

    // 로딩 화면이므로 디버그 정보는 깔끔하게 끕니다.
    override val drawsDebugInfo = false
    override val drawsFpsGraph = false

    override fun createRootScene(gctx: GameContext): Scene {
        // MainActivity에서 전달받은 목적지를 꺼냅니다. (없으면 MAIN을 기본값으로)
        val destination = intent.getStringExtra("DESTINATION") ?: "MAIN"

        // 목적지 정보를 LoadingScene으로 전달하면서 화면을 생성합니다.
        return LoadingScene(gctx, this, destination)
    }
}