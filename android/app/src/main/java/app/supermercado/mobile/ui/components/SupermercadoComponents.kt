package app.supermercado.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.supermercado.mobile.ui.theme.PillShape
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

/** Badge em formato de pílula — porta `.category-badge`/`.category-total`/`.result-badge` do CSS. */
@Composable
fun PillBadge(
    texto: String,
    modifier: Modifier = Modifier,
    contentColor: Color = SupermercadoColorTokens.onSurfaceMuted,
    containerColor: Color = contentColor.copy(alpha = 0.10f),
    borderColor: Color? = null,
) {
    Surface(
        modifier = modifier,
        shape = PillShape,
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

/** Contador de quantidade em pílula com botões circulares — porta `.qtd` do CSS. */
@Composable
fun QtyStepper(
    qtd: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = PillShape,
        color = SupermercadoColorTokens.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StepperButton(icon = Icons.Filled.Remove, contentDescription = "Diminuir", onClick = onDecrement)
            Text(
                qtd.toString(),
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            StepperButton(icon = Icons.Filled.Add, contentDescription = "Aumentar", onClick = onIncrement)
        }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = PillShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(16.dp))
    }
}
