package pe.upeu.biblioandes.presentation.prestamos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.presentation.components.ChipsDeFiltro
import pe.upeu.biblioandes.presentation.components.DialogoConfirmacion
import pe.upeu.biblioandes.presentation.components.EstadoCargando
import pe.upeu.biblioandes.presentation.components.EstadoError
import pe.upeu.biblioandes.presentation.components.EstadoVacio
import pe.upeu.biblioandes.presentation.components.PrestamoItem
import pe.upeu.biblioandes.presentation.prestamos.PrestamosUiState.Fase

@Composable
fun PrestamosRoute(
    viewModel: PrestamosViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    PrestamosScreen(
        uiState = uiState,
        onFiltroSeleccionado = viewModel::onFiltroSeleccionado,
        onDevolver = viewModel::onDevolverPulsado,
        onConfirmarDevolucion = viewModel::onDevolucionConfirmada,
        onCancelarDevolucion = viewModel::onDevolucionCancelada,
        onReintentar = viewModel::cargar
    )
}

@Composable
fun PrestamosScreen(
    uiState: PrestamosUiState,
    onFiltroSeleccionado: (FiltroDeEstado) -> Unit,
    onDevolver: (Prestamo) -> Unit,
    onConfirmarDevolucion: () -> Unit,
    onCancelarDevolucion: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        ChipsDeFiltro(
            opciones = FiltroDeEstado.entries,
            seleccionada = uiState.filtro,
            etiqueta = { filtro -> filtro.etiqueta },
            onSeleccionar = onFiltroSeleccionado,
            modifier = Modifier.padding(top = 8.dp)
        )

        when (val fase = uiState.fase) {

            Fase.Cargando -> EstadoCargando(mensaje = "Cargando tus préstamos…")

            is Fase.Error -> EstadoError(
                mensaje = fase.mensaje,
                onReintentar = onReintentar
            )

            Fase.Vacio -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EstadoVacio(
                    icono = Icons.Default.Inbox,
                    titulo = "Nada por aquí",
                    descripcion = "No tienes préstamos en este estado."
                )
            }

            is Fase.Contenido -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fase.prestamos, key = { it.id }) { prestamo ->
                    PrestamoItem(
                        prestamo = prestamo,
                        onDevolver = { onDevolver(prestamo) }
                    )
                }
            }
        }
    }

    val porDevolver = uiState.porDevolver
    if (porDevolver != null) {
        DialogoConfirmacion(
            titulo = "¿Registrar la devolución?",
            mensaje = "Se marcará «${porDevolver.libro.titulo}» como devuelto con fecha de hoy.",
            textoConfirmar = "Registrar",
            onConfirmar = onConfirmarDevolucion,
            onCancelar = onCancelarDevolucion
        )
    }
}
