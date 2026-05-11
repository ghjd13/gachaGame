package com.example.gachagame

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class MainActivity : BaseGameActivity() {

    // 디버그 정보를 화면에 띄울지 여부 (필요에 따라 true/false 변경)
    override val drawsDebugInfo = false
    override val drawsFpsGraph = false

    // BaseGameActivity의 필수 구현 메서드: 첫 화면(Scene)을 무엇으로 할지 결정합니다.
    override fun createRootScene(gctx: GameContext): Scene {
        // 게임 초기 데이터 세팅 (기존 코드 유지)
        CharacterDataManager.initDataIfNeeded(this)

        // 로비 씬(화면)을 생성하여 반환합니다.
        return LobbyScene(gctx, this)
    }
}