package pe.upeu.biblioandes.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.presentation.components.EstadoCargando
import pe.upeu.biblioandes.presentation.components.EstadoError
import pe.upeu.biblioandes.presentation.components.Etiqueta
import pe.upeu.biblioandes.presentation.components.descripcion
import pe.upeu.biblioandes.presentation.components.fechaCorta
import pe.upeu.biblioandes.presentation.inicio.InicioUiState.Fase

@Composable
fun InicioRoute(
    onIrAlCatalogo: () -> Unit,
    onIrAPrestamos: () -> Unit,
    viewModel: InicioViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    InicioScreen(
        uiState = uiState,
        onIrAlCatalogo = onIrAlCatalogo,
        onIrAPrestamos = onIrAPrestamos,
        onReintentar = viewModel::cargar
    )
}

@Composable
fun InicioScreen(
    uiState: InicioUiState,
    onIrAlCatalogo: () -> Unit,
    onIrAPrestamos: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (val fase = uiState.fase) {

        Fase.Cargando -> EstadoCargando(
            mensaje = "Preparando tu biblioteca…",
            modifier = modifier
        )

        is Fase.Error -> EstadoError(
            mensaje = fase.mensaje,
            onReintentar = onReintentar,
            modifier = modifier
        )

        is Fase.Contenido -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Saludo(estudiante = fase.estudiante)

            ProximaDevolucion(
                prestamo = fase.proximaDevolucion,
                onClick = onIrAPrestamos
            )

            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccesoRapido(
                    icono = Icons.AutoMirrored.Filled.MenuBook,
                    titulo = "Catálogo",
                    descripcion = "Busca y solicita libros",
                    onClick = onIrAlCatalogo,
                    modifier = Modifier.weight(1f)
                )
                AccesoRapido(
                    icono = Icons.Default.Bookmarks,
                    titulo = "Mis préstamos",
                    descripcion = "Revisa tus fechas",
                    onClick = onIrAPrestamos,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Saludo(estudiante: Estudiante) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            // Solo el primer nombre: "Diego Huamán Ccama" se saluda como "Diego".
            text = "Hola, ${estudiante.nombre.substringBefore(' ')}",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "${estudiante.carrera} · ${estudiante.codigo}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Tarjeta destacada: el prestamo cuya devolucion vence primero. */
@Composable
private fun ProximaDevolucion(
    prestamo: Prestamo?,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val vencido = prestamo?.estado is EstadoPrestamo.Vencido

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (vencido) colores.errorContainer else colores.primaryContainer,
            contentColor = if (vencido) colores.onErrorContainer else colores.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tu próxima devolución",
                style = MaterialTheme.typography.labelLarge
            )

            if (prestamo == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null)
                    Text(
                        text = "No tienes devoluciones pendientes.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    text = prestamo.libro.titulo,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Devolver hasta el ${fechaCorta(prestamo.fechaLimite)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Sobre una tarjeta de color la etiqueta va en tono neutro para que se lea.
                Etiqueta(
                    texto = prestamo.estado.descripcion(),
                    fondo = colores.surface,
                    colorTexto = colores.onSurface
                )
            }
        }
    }
}

@Composable
private fun AccesoRapido(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
