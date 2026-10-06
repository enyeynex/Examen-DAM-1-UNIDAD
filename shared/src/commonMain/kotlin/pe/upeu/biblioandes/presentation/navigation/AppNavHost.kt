package pe.upeu.biblioandes.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import pe.upeu.biblioandes.presentation.catalogo.CatalogoRoute
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroRoute
import pe.upeu.biblioandes.presentation.inicio.InicioRoute
import pe.upeu.biblioandes.presentation.perfil.PerfilRoute
import pe.upeu.biblioandes.presentation.prestamos.PrestamosRoute

/**
 * Esqueleto de la aplicacion: un Scaffold con barra superior e inferior y,
 * dentro, el NavHost que decide que pantalla se ve.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    modoOscuro: Boolean,
    onModoOscuroCambia: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    // La ruta visible se lee de la pila de navegacion; cuando cambia, el
    // Scaffold se recompone y actualiza titulo y barras.
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route ?: Destinos.INICIO
    val enDestinoPrincipal = DESTINOS_PRINCIPALES.any { it.ruta == rutaActual }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Destinos.tituloDe(rutaActual)) },
                navigationIcon = {
                    // Detalle y Perfil se abren encima de otra pantalla: llevan flecha de retorno.
                    if (!enDestinoPrincipal) {
                        IconButton(onClick = { navController.volver() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                actions = {
                    if (enDestinoPrincipal) {
                        IconButton(onClick = { navController.navigate(Destinos.PERFIL) }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil y ajustes"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (enDestinoPrincipal) {
                NavigationBar {
                    DESTINOS_PRINCIPALES.forEach { destino ->
                        NavigationBarItem(
                            selected = destino.ruta == rutaActual,
                            onClick = { navController.irADestinoPrincipal(destino.ruta) },
                            icon = { Icon(destino.icono, contentDescription = null) },
                            label = { Text(destino.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { relleno ->

        NavHost(
            navController = navController,
            startDestination = Destinos.INICIO,
            modifier = Modifier.padding(relleno)
        ) {

            composable(Destinos.INICIO) {
                InicioRoute(
                    onIrAlCatalogo = { navController.irADestinoPrincipal(Destinos.CATALOGO) },
                    onIrAPrestamos = { navController.irADestinoPrincipal(Destinos.PRESTAMOS) }
                )
            }

            composable(Destinos.CATALOGO) {
                CatalogoRoute(
                    onAbrirLibro = { libroId -> navController.navigate(Destinos.detalle(libroId)) }
                )
            }

            composable(
                route = Destinos.DETALLE,
                arguments = listOf(
                    navArgument(Destinos.ARG_LIBRO_ID) { type = NavType.IntType }
                )
            ) { entrada ->
                // El id del libro viaja dentro de la ruta: "detalle/4" -> 4.
                val libroId = entrada.arguments?.read { getInt(Destinos.ARG_LIBRO_ID) }
                if (libroId != null) {
                    DetalleLibroRoute(libroId = libroId)
                }
            }

            composable(Destinos.PRESTAMOS) {
                PrestamosRoute()
            }

            composable(Destinos.PERFIL) {
                PerfilRoute(
                    modoOscuro = modoOscuro,
                    onModoOscuroCambia = onModoOscuroCambia
                )
            }
        }
    }
}

/**
 * Cambia de pestana sin apilar pantallas: se vuelve hasta Inicio guardando el
 * estado de la pestana que se deja, asi el boton atras siempre regresa a
 * Inicio y, desde ahi, sale de la aplicacion.
 */
private fun NavHostController.irADestinoPrincipal(ruta: String) {
    navigate(ruta) {
        popUpTo(Destinos.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Retrocede una pantalla, salvo que ya no quede ninguna detras. */
private fun NavHostController.volver() {
    if (previousBackStackEntry != null) {
        popBackStack()
    }
}
