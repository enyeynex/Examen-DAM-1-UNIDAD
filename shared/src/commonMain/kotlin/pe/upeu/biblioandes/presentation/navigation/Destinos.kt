package pe.upeu.biblioandes.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rutas de la aplicacion como constantes: ninguna pantalla escribe una ruta
 * a mano, todas pasan por aqui.
 */
object Destinos {

    const val INICIO = "inicio"
    const val CATALOGO = "catalogo"
    const val PRESTAMOS = "prestamos"
    const val PERFIL = "perfil"

    /** Nombre del argumento que viaja en la ruta del detalle. */
    const val ARG_LIBRO_ID = "libroId"
    const val DETALLE = "detalle/{$ARG_LIBRO_ID}"

    /** Arma la ruta del detalle de un libro concreto, por ejemplo "detalle/4". */
    fun detalle(libroId: Int): String = "detalle/$libroId"

    /** Titulo de la barra superior segun la pantalla visible. */
    fun tituloDe(ruta: String): String = when (ruta) {
        INICIO -> "BiblioAndes"
        CATALOGO -> "Catálogo"
        PRESTAMOS -> "Mis préstamos"
        PERFIL -> "Perfil y ajustes"
        DETALLE -> "Detalle del libro"
        else -> "BiblioAndes"
    }
}

/** Un destino de la barra de navegacion inferior. */
data class DestinoPrincipal(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

/** Los tres destinos de la barra inferior, en el orden en que se muestran. */
val DESTINOS_PRINCIPALES = listOf(
    DestinoPrincipal(Destinos.INICIO, "Inicio", Icons.Default.Home),
    DestinoPrincipal(Destinos.CATALOGO, "Catálogo", Icons.AutoMirrored.Filled.MenuBook),
    DestinoPrincipal(Destinos.PRESTAMOS, "Préstamos", Icons.Default.Bookmarks)
)
