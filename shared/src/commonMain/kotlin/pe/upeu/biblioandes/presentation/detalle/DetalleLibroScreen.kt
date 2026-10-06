package pe.upeu.biblioandes.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.upeu.biblioandes.domain.model.DetalleLibro
import pe.upeu.biblioandes.domain.model.PoliticaPrestamo
import pe.upeu.biblioandes.presentation.components.DialogoConfirmacion
import pe.upeu.biblioandes.presentation.components.EstadoCargando
import pe.upeu.biblioandes.presentation.components.EstadoError
import pe.upeu.biblioandes.presentation.components.EtiquetaDisponibilidad
import pe.upeu.biblioandes.presentation.components.FilaDeDato
import pe.upeu.biblioandes.presentation.components.PortadaLibro
import pe.upeu.biblioandes.presentation.components.explicacion
import pe.upeu.biblioandes.presentation.components.fechaCorta
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroUiState.Aviso
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroUiState.Fase

@Composable
fun DetalleLibroRoute(
    libroId: Int,
    // El id llega por la ruta de navegacion y Koin se lo entrega al ViewModel.
    viewModel: DetalleLibroViewModel = koinViewModel { parametersOf(libroId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    DetalleLibroScreen(
        uiState = uiState,
        onSolicitar = viewModel::onSolicitarPulsado,
        onConfirmar = viewModel::onSolicitudConfirmada,
        onCancelar = viewModel::onSolicitudCancelada,
        onReintentar = viewModel::cargar
    )
}

@Composable
fun DetalleLibroScreen(
    uiState: DetalleLibroUiState,
    onSolicitar: () -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (val fase = uiState.fase) {

        Fase.Cargando -> EstadoCargando(
            mensaje = "Cargando el libro…",
            modifier = modifier
        )

        is Fase.Error -> EstadoError(
            mensaje = fase.mensaje,
            onReintentar = onReintentar,
            modifier = modifier
        )

        is Fase.Contenido -> {
            ContenidoDelLibro(
                detalle = fase.detalle,
                solicitando = uiState.solicitando,
                aviso = uiState.aviso,
                onSolicitar = onSolicitar,
                modifier = modifier
            )

            if (uiState.confirmando) {
                DialogoConfirmacion(
                    titulo = "¿Solicitar este libro?",
                    mensaje = "Vas a pedir «${fase.detalle.libro.titulo}». El préstamo dura " +
                        "${PoliticaPrestamo.DIAS_DE_PRESTAMO} días desde hoy.",
                    textoConfirmar = "Solicitar",
                    onConfirmar = onConfirmar,
                    onCancelar = onCancelar
                )
            }
        }
    }
}

@Composable
private fun ContenidoDelLibro(
    detalle: DetalleLibro,
    solicitando: Boolean,
    aviso: Aviso?,
    onSolicitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libro = detalle.libro

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PortadaLibro(modifier = Modifier.size(72.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = libro.titulo,
                    style = MaterialTheme.typography.headlineSmall
                )
                EtiquetaDisponibilidad(ejemplares = libro.ejemplaresDisponibles)
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                FilaDeDato(Icons.Default.Person, "Autor", libro.autor)
                HorizontalDivider()
                FilaDeDato(Icons.Default.CalendarMonth, "Año", libro.anio.toString())
                HorizontalDivider()
                FilaDeDato(Icons.Default.Category, "Categoría", libro.categoria)
                HorizontalDivider()
                FilaDeDato(Icons.Default.LocationOn, "Sede", libro.sede)
                HorizontalDivider()
                FilaDeDato(
                    Icons.Default.Inventory2,
                    "Ejemplares disponibles",
                    libro.ejemplaresDisponibles.toString()
                )
            }
        }

        if (aviso != null) {
            TarjetaDeAviso(aviso)
        }

        // La pantalla no evalua ninguna regla: el dominio ya dijo si se puede
        // solicitar y, cuando no, por que motivo.
        val motivo = detalle.motivoDeBloqueo
        if (motivo != null && aviso !is Aviso.Rechazado) {
            Text(
                text = motivo.explicacion(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onSolicitar,
            enabled = detalle.sePuedeSolicitar && !solicitando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (solicitando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Solicitar préstamo")
            }
        }
    }
}

@Composable
private fun TarjetaDeAviso(aviso: Aviso) {

    val colores = MaterialTheme.colorScheme

    val texto: String
    val fondo: Color
    val colorTexto: Color

    when (aviso) {
        is Aviso.PrestamoRegistrado -> {
            texto = "Préstamo registrado. Devuelve el libro hasta el ${fechaCorta(aviso.fechaLimite)}."
            fondo = colores.tertiaryContainer
            colorTexto = colores.onTertiaryContainer
        }
        is Aviso.Rechazado -> {
            texto = aviso.motivo.explicacion()
            fondo = colores.errorContainer
            colorTexto = colores.onErrorContainer
        }
        is Aviso.Fallo -> {
            texto = aviso.mensaje
            fondo = colores.errorContainer
            colorTexto = colores.onErrorContainer
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = colorTexto)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}
