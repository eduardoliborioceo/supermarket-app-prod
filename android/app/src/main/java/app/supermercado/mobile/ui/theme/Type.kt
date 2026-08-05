package app.supermercado.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Escala tipografica alinhada ao font stack do web (Inter/system-ui) e aos
 * tamanhos ja usados em CLAUDE.md (page header name 1.05rem/700, card title
 * 0.875-1.05rem/600-700, body 0.875rem/500, form label 0.8rem/600, etc).
 * FontFamily.Default cai no stack padrao da plataforma; trocar por uma fonte
 * Inter empacotada deixaria os dois frontends visualmente identicos.
 */
private val SupermercadoFontFamily = FontFamily.Default

val SupermercadoTypography = Typography(
    displayLarge = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp),
    headlineLarge = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = SupermercadoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp),
)
