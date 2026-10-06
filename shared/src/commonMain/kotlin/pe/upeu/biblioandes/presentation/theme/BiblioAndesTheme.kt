package pe.upeu.biblioandes.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Tema Material 3 de la aplicacion. Recibe [modoOscuro] como parametro: no
 * decide por su cuenta, quien lo llama le dice que esquema usar.
 */
@Composable
fun BiblioAndesTheme(
    modoOscuro: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
        typography = TipografiaBiblioAndes,
        content = content
    )
}
