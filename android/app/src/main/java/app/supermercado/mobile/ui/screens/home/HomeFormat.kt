package app.supermercado.mobile.ui.screens.home

import java.util.Locale

fun formatarMoeda(valor: Double): String =
    "R$ " + String.format(Locale("pt", "BR"), "%.2f", valor).replace('.', ',')

fun parseMoeda(texto: String): Double {
    val normalizado = texto.trim().replace("R$", "").replace(" ", "").replace(",", ".")
    return normalizado.toDoubleOrNull() ?: 0.0
}
