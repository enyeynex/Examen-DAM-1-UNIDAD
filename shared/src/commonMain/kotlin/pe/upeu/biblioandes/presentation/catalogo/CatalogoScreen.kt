package pe.upeu.biblioandes.presentation.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.upeu.biblioandes.presentation.catalogo.CatalogoUiState.Fase
import pe.upeu.biblioandes.presentation.components.ChipsDeFiltro
import pe.upeu.biblioandes.presentation.components.EstadoCargando
import pe.upeu.biblioandes.presentation.components.EstadoError
import pe.upeu.biblioandes.presentation.components.EstadoVacio
import pe.upeu.biblioandes.presentation.components.LibroItem

/**
 * Conecta la pantalla con su ViewModel: observa el estado y le pasa los
 * eventos. La pantalla de abajo no conoce al ViewModel.
 */
@Composable
fun CatalogoRoute(
    onAbrirLibro: (Int) -> Unit,
    viewModel: CatalogoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    CatalogoScreen(
        uiState = uiState,
        onBusquedaCambia = viewModel::onBusquedaCambia,
        onCategoriaSeleccionada = viewModel::onCategoriaSeleccionada,
        onAbrirLibro = onAbrirLibro,
        onReintentar = viewModel::cargar
    )
}

/**
 * Pantalla sin estado propio (state hoisting): dibuja lo que dice [uiState]
 * y avisa de cada accion con una funcion. No filtra ni decide nada.
 */
@Composable
fun CatalogoScreen(
    uiState: CatalogoUiState,
    onBusquedaCambia: (String) -> Unit,
    onCategoriaSeleccionada: (String?) -> Unit,
    onAbrirLibro: (Int) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        CampoDeBusqueda(
            texto = uiState.busqueda,
            onTextoCambia = onBusquedaCambia,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        // "Todas" se representa con null, seguido de las categorias reales.
        ChipsDeFiltro(
            opciones = listOf<String?>(null) + uiState.categorias,
            seleccionada = uiState.categoriaSeleccionada,
            etiqueta = { categoria -> categoria ?: "Todas" },
            onSeleccionar = onCategoriaSeleccionada
        )

        when (val fase = uiState.fase) {

            Fase.Cargando -> EstadoCargando(mensaje = "Cargando el catálogo…")

            is Fase.Error -> EstadoError(
                mensaje = fase.mensaje,
                onReintentar = onReintentar
            )

            Fase.SinResultados -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EstadoVacio(
                    icono = Icons.Default.SearchOff,
                    titulo = "Ningún libro coincide",
                    descripcion = "Prueba con otra categoría o cambia el texto de búsqueda."
                )
            }

            is Fase.Contenido -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fase.libros, key = { it.id }) { libro ->
                    LibroItem(
                        libro = libro,
                        onClick = { onAbrirLibro(libro.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoDeBusqueda(
    texto: String,
    onTextoCambia: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoCambia,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("Buscar por título o autor") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoCambia("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                }
            }
        }
    )
}
