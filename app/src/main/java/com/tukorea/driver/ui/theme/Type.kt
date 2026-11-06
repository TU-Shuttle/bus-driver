package com.tukorea.driver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import com.tukorea.driver.R

// Font Family
val PretendardFontFamily =
    FontFamily(
        Font(R.font.pretendard_thin, FontWeight.Thin),
        Font(R.font.pretendard_extralight, FontWeight.ExtraLight),
        Font(R.font.pretendard_light, FontWeight.Light),
        Font(R.font.pretendard_regular, FontWeight.Normal),
        Font(R.font.pretendard_medium, FontWeight.Medium),
        Font(R.font.pretendard_semibold, FontWeight.SemiBold),
        Font(R.font.pretendard_bold, FontWeight.Bold),
        Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
    )

/*
 * 타이포그래피 규칙
 * - 한글은 자간이 넓으면 엉성해 보여서 자간은 0 이하로 둔다(헤드라인 -0.2, 디스플레이 -1).
 * - 시각이 흔들리지 않도록 모든 스타일에 고정폭 숫자(tnum)를 쓴다.
 * - 굵기는 스타일에 들어 있다. 화면에서 fontSize 를 직접 주지 않는다.
 */
private const val TABULAR_NUMBERS = "tnum"

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Float = 0f,
) = TextStyle(
    fontFamily = PretendardFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
    // 한국어 전용 앱이라 기기 언어와 상관없이 한국어 규칙(어절 단위 줄바꿈)을 쓴다.
    localeList = LocaleList("ko-KR"),
    // 한글이 단어 중간에서 끊기지 않게 어절 단위로 줄바꿈한다(API 33+, 그 아래에선 무시됨).
    lineBreak =
        LineBreak(
            strategy = LineBreak.Strategy.HighQuality,
            strictness = LineBreak.Strictness.Strict,
            wordBreak = LineBreak.WordBreak.Phrase,
        ),
)

val AppTypography =
    Typography(
        displayLarge = style(56, 60, FontWeight.ExtraBold, -1f), // 다음 셔틀 시각/남은 분
        displayMedium = style(40, 48, FontWeight.Bold, -1f),
        displaySmall = style(32, 40, FontWeight.Bold, -0.5f),
        headlineLarge = style(24, 32, FontWeight.Bold, -0.2f), // 화면 제목
        headlineMedium = style(22, 30, FontWeight.Bold, -0.2f),
        headlineSmall = style(20, 28, FontWeight.Bold, -0.2f), // 시트 제목, 큰 시각
        titleLarge = style(18, 26, FontWeight.Bold), // 섹션 제목
        titleMedium = style(16, 24, FontWeight.SemiBold), // 행 제목, 버튼
        titleSmall = style(14, 20, FontWeight.SemiBold), // 섹션 라벨
        bodyLarge = style(16, 24, FontWeight.Normal),
        bodyMedium = style(14, 22, FontWeight.Normal),
        bodySmall = style(12, 18, FontWeight.Normal), // 캡션
        labelLarge = style(14, 20, FontWeight.SemiBold), // 칩, 알약 버튼
        labelMedium = style(12, 16, FontWeight.SemiBold),
        labelSmall = style(11, 16, FontWeight.Medium),
    )
