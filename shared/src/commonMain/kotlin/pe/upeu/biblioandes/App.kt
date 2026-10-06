package pe.upeu.biblioandes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import pe.upeu.biblioandes.presentation.navigation.AppNavHost
import pe.upeu.biblioandes.presentation.theme.BiblioAndesTheme

/**
 * Raiz de la interfaz, compartida por Android e iOS.
 *
 * El estado del tema vive aqui, en lo mas alto del arbol de composicion:
 * BiblioAndesTheme envuelve a toda la aplicacion, por eso al cambiar el
 * interruptor en Perfil se recompone todo con el nuevo esquema de colores.
 *
 * [onModoOscuroAplicado] avisa a la plataforma cuando cambia el tema, para
 * que ajuste lo que queda fuera de Compose (los iconos de la barra de estado).
 */
@Composable
fun App(
    onModoOscuroAplicado: (Boolean) -> Unit = {}
) {
    // null = el estudiante aun no eligio: se sigue el tema del telefono.
    var preferenciaDeTema by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val modoOscuro = preferenciaDeTema ?: isSystemInDarkTheme()

    LaunchedEffect(modoOscuro) { onModoOscuroAplicado(modoOscuro) }

    BiblioAndesTheme(modoOscuro = modoOscuro) {
        AppNavHost(
            modoOscuro = modoOscuro,
            onModoOscuroCambia = { elegido -> preferenciaDeTema = elegido }
        )
    }
}
