package app.supermercado.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SupermercadoLightColorScheme = lightColorScheme(
    primary = SupermercadoColorTokens.primary,
    background = SupermercadoColorTokens.background,
    surface = SupermercadoColorTokens.surface,
    onBackground = SupermercadoColorTokens.onSurface,
    onSurface = SupermercadoColorTokens.onSurface,
    error = SupermercadoColorTokens.error,
    outline = SupermercadoColorTokens.border,
)

private val SupermercadoDarkColorScheme = darkColorScheme(
    primary = SupermercadoColorTokens.primary,
    background = SupermercadoColorTokens.sidebarBackground,
    surface = SupermercadoColorTokens.sidebarBackground,
    onBackground = SupermercadoColorTokens.sidebarTextActive,
    onSurface = SupermercadoColorTokens.sidebarTextActive,
    error = SupermercadoColorTokens.error,
    outline = SupermercadoColorTokens.sidebarBorder,
)

/**
 * Tema Material 3 raiz. Nao usa dynamic color (Android 12+) de proposito —
 * o app tem paleta de marca propria compartilhada com o web, mesma logica do
 * design system unico definido em CLAUDE.md.
 */
@Composable
fun SupermercadoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SupermercadoDarkColorScheme else SupermercadoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SupermercadoTypography,
        shapes = SupermercadoShapes,
        content = content,
    )
}
