package kr.ac.tukorea.ge.spgp2026.a2dg.objects

import android.graphics.Canvas
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// VertScrollBackground를 기반으로 작성된 가로 스크롤 배경 클래스입니다.
// x를 "배경이 가로로 얼마나 이동했는가"를 나타내는 스크롤 양으로 사용하며,
// draw()에서는 현재 스크롤 위치를 tileWidth로 나눈 나머지를 기준으로
// 같은 bitmap을 가로로 여러 번 반복해서 이어 그립니다.
open class HorzScrollBackground(
    gctx: GameContext,
    resId: Int,
    private val speed: Float,
) : Sprite(gctx, resId) {
    private val screenWidth = gctx.metrics.width
    private val screenHeight = gctx.metrics.height
    // 배경 이미지를 화면 세로 높이에 맞췄을 때의 가로 길이를 계산합니다.
    private val tileWidth = bitmapWidth * screenHeight / bitmapHeight.toFloat()

    init {
        // 배경 이미지는 화면 세로 폭에 맞춘 채 원본 비율을 유지합니다.
        // 기존 Sprite에 setCenterProportionalHeight와 같은 메서드가 있다면 사용할 수 있으며,
        // 여기서는 기본적으로 x, y의 중심점을 화면 중앙으로 초기화합니다.
        x = screenWidth / 2f
        y = screenHeight / 2f
    }

    override fun update(gctx: GameContext) {
        // x 값을 중심점이 아니라 누적 스크롤 양으로 사용합니다.
        // 배경 자체를 이동시키지 않고 "어디서부터 반복 배치를 시작할지"를 바꿉니다.
        x += speed * gctx.frameTime
    }

    override fun draw(canvas: Canvas) {
        var curr = x % tileWidth
        // 스크롤 방향에 따라 화면 밖(왼쪽)에서부터 자연스럽게 이어지도록 처리합니다.
        if (curr > 0f) curr -= tileWidth

        while (curr < screenWidth) {
            // dstRect를 가로로 배치 (left, top, right, bottom)
            dstRect.set(curr, 0f, curr + tileWidth, screenHeight)
            canvas.drawBitmap(bitmap, null, dstRect, null)
            curr += tileWidth
        }
    }
}