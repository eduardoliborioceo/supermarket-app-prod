package app.supermercado.mobile.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarProdutoDialog(
    categorias: List<String>,
    categoriaInicial: String?,
    onDismiss: () -> Unit,
    onConfirmar: (nome: String, preco: Double, setor: String) -> Unit,
) {
    var nome by remember { mutableStateOf("") }
    var precoTexto by remember { mutableStateOf("") }
    var setorSelecionado by remember { mutableStateOf(categoriaInicial ?: categorias.firstOrNull().orEmpty()) }
    var menuAberto by remember { mutableStateOf(false) }
    var erroValidacao by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo produto") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do produto") },
                    placeholder = { Text("Ex: Arroz") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = precoTexto,
                    onValueChange = { precoTexto = it },
                    label = { Text("Preço") },
                    placeholder = { Text("Ex: 12,90") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                ExposedDropdownMenuBox(expanded = menuAberto, onExpandedChange = { menuAberto = it }) {
                    OutlinedTextField(
                        value = setorSelecionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAberto) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(text = { Text(categoria) }, onClick = {
                                setorSelecionado = categoria
                                menuAberto = false
                            })
                        }
                    }
                }
                erroValidacao?.let {
                    Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val preco = parseMoeda(precoTexto)
                if (nome.isBlank() || preco <= 0.0) {
                    erroValidacao = "Informe um nome válido e preço maior que zero."
                    return@TextButton
                }
                onConfirmar(nome.trim(), preco, setorSelecionado)
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
