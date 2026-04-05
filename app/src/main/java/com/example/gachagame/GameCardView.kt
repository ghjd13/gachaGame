package com.example.gachagame // 본인 프로젝트의 패키지명으로 변경하세요

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class GameCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // XML Attributes
    private var cardCostValue: Int = 0
    private var cardCostColor: Int = Color.BLUE
    private var cardNameText: String = "이름 없음"
    private var cardDescriptionText: String = ""
    private var cardImageSrc: Drawable? = null

    // Paints
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f // 두께 5px
    }

    private val costCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val costTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // StaticLayout에서 사용하기 위해 TextPaint로 변경
    private val descPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        // StaticLayout 내부에서 정렬하므로 textAlign은 설정하지 않음
    }

    private val fallbackImagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY // 회색 빈 사각형 기본값
        style = Paint.Style.FILL
    }

    init {
        // attrs.xml에서 정의한 속성값 가져오기
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.GameCardView, 0, 0)

            cardCostValue = typedArray.getInt(R.styleable.GameCardView_cardCostValue, 0)
            cardCostColor = typedArray.getColor(R.styleable.GameCardView_cardCostColor, Color.BLUE)
            cardNameText = typedArray.getString(R.styleable.GameCardView_cardNameText) ?: "이름 없음"
            cardDescriptionText = typedArray.getString(R.styleable.GameCardView_cardDescriptionText) ?: ""
            cardImageSrc = typedArray.getDrawable(R.styleable.GameCardView_cardImageSrc)

            typedArray.recycle()
        }

        costCirclePaint.color = cardCostColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 4방향 Padding을 고려하여 실제 그려질 영역 계산
        val drawWidth = width - paddingLeft - paddingRight
        val drawHeight = height - paddingTop - paddingBottom

        if (drawWidth <= 0 || drawHeight <= 0) return

        val w = drawWidth.toFloat()
        val h = drawHeight.toFloat()
        val minDim = min(w, h)

        // Canvas의 원점을 패딩이 적용된 좌상단으로 이동 (좌표 계산을 쉽게 하기 위함)
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

        // 1. 카드 배경 및 테두리
        val cornerRadius = minDim / 10f
        // 테두리가 잘리지 않도록 strokeWidth/2 만큼 안쪽으로 사각형 영역 지정
        val inset = borderPaint.strokeWidth / 2f
        val cardRect = RectF(inset, inset, w - inset, h - inset)

        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)

        // 2. 카드 이미지 영역 (상단 중앙)
        val imgLeft = w * 0.1f
        val imgTop = h * 0.2f
        val imgRight = w * 0.9f
        val imgBottom = h * 0.5f

        val targetImgWidth = imgRight - imgLeft
        val targetImgHeight = imgBottom - imgTop

        if (cardImageSrc != null) {
            val drawable = cardImageSrc!!
            val intrinsicW = drawable.intrinsicWidth
            val intrinsicH = drawable.intrinsicHeight

            if (intrinsicW > 0 && intrinsicH > 0) {
                // 이미지가 늘어지지 않도록 비율 유지 (FIT_CENTER 형태)
                val scale = min(targetImgWidth / intrinsicW, targetImgHeight / intrinsicH)
                val drawW = intrinsicW * scale
                val drawH = intrinsicH * scale

                // 중앙 정렬을 위한 여백 계산
                val drawL = imgLeft + (targetImgWidth - drawW) / 2f
                val drawT = imgTop + (targetImgHeight - drawH) / 2f

                drawable.setBounds(
                    drawL.toInt(),
                    drawT.toInt(),
                    (drawL + drawW).toInt(),
                    (drawT + drawH).toInt()
                )
            } else {
                // 고유 크기가 없는 경우 (예: ColorDrawable) 영역을 꽉 채움
                drawable.setBounds(imgLeft.toInt(), imgTop.toInt(), imgRight.toInt(), imgBottom.toInt())
            }
            drawable.draw(canvas)
        } else {
            // 지정된 이미지가 없는 경우 회색 사각형
            canvas.drawRect(imgLeft, imgTop, imgRight, imgBottom, fallbackImagePaint)
        }

        // 3. 코스트 (Cost) 원형 아이콘
        val cx = w / 8f
        val cy = h / 10f
        val radius = minDim / 8f
        canvas.drawCircle(cx, cy, radius, costCirclePaint)

        // 코스트 텍스트 그리기
        costTextPaint.textSize = radius * 1.5f
        val costTextY = cy - (costTextPaint.descent() + costTextPaint.ascent()) / 2f
        canvas.drawText(cardCostValue.toString(), cx, costTextY, costTextPaint)

        // 4. 카드 이름 (Title)
        val titleX = w / 2f
        val titleY = h * 0.6f
        titlePaint.textSize = h / 15f
        // 기준 위치를 수직 중앙으로 맞춤
        val titleDrawY = titleY - (titlePaint.descent() + titlePaint.ascent()) / 2f
        canvas.drawText(cardNameText, titleX, titleDrawY, titlePaint)

        // 5. 카드 설명 (Description) - 긴 글자는 자동으로 줄바꿈되도록 StaticLayout 적용
        val descY = h * 0.7f
        descPaint.textSize = h / 25f
        val maxTextWidth = (w * 0.9f).toInt() // 양옆 여백을 남기고 텍스트 너비 지정

        // 버전별로 StaticLayout 생성 방식 분기
        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(cardDescriptionText, 0, cardDescriptionText.length, descPaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER) // 가운데 정렬
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(cardDescriptionText, descPaint, maxTextWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)
        }

        canvas.save()
        // StaticLayout은 시작점(0)을 기준으로 그리므로 뷰의 중앙에 오도록 x축을 translate 해줍니다.
        canvas.translate(w / 2f - maxTextWidth / 2f, descY)
        staticLayout.draw(canvas)
        canvas.restore()

        // 원점 복구
        canvas.restore()
    }
}