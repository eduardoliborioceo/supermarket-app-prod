package app.supermercado.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens ported 1:1 from o design system web existente (CLAUDE.md >
 * "UI Layout Design System" e os tokens `:root` de `app/static/css`), para o
 * app nativo e o app web Flask/Jinja2 renderizarem a mesma marca.
 */
object SupermercadoColorTokens {
    val background = Color(0xFFF1F5F9)
    val surface = Color(0xFFFFFFFF)
    val onSurface = Color(0xFF1E293B)
    val onSurfaceMuted = Color(0xFF64748B)
    val border = Color(0x170F172A)
    val primary = Color(0xFF0D6EFD)
    val success = Color(0xFF198754)
    val error = Color(0xFFDC3545)
    val warning = Color(0xFFFFC107)
    val info = Color(0xFF0DCAF0)

    val sidebarBackground = Color(0xFF0F172A)
    val sidebarBorder = Color(0x12FFFFFF)
    val sidebarText = Color(0x8CFFFFFF)
    val sidebarTextActive = Color(0xFFFFFFFF)
    val sidebarActiveAccent = Color(0xFF3B82F6)
    val sidebarActiveBackground = Color(0x293B82F6)
}
