package pe.upeu.biblioandes.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.presentation.components.EstadoCargando
import pe.upeu.biblioandes.presentation.components.EstadoError
import pe.upeu.biblioandes.presentation.components.FilaDeDato
import pe.upeu.biblioandes.presentation.perfil.PerfilUiState.Fase

@Composable
fun PerfilRoute(
    modoOscuro: Boolean,
    onModoOscuroCambia: (Boolean) -> Unit,
    viewModel: PerfilViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    PerfilScreen(
        uiState = uiState,
        modoOscuro = modoOscuro,
        onModoOscuroCambia = onModoOscuroCambia,
        onReintentar = viewModel::cargar
    )
}

/**
 * El interruptor del tema no guarda nada aqui: recibe [modoOscuro] y avisa
 * del cambio con [onModoOscuroCambia]. El estado vive arriba, en App.
 */
@Composable
fun PerfilScreen(
    uiState: PerfilUiState,
    modoOscuro: Boolean,
    onModoOscuroCambia: (Boolean) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (val fase = uiState.fase) {

        Fase.Cargando -> EstadoCargando(
            mensaje = "Cargando tu perfil…",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DatosDelEstudiante(estudiante = fase.estudiante)

            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.titleMedium
            )

            InterruptorDeTema(
                modoOscuro = modoOscuro,
                onModoOscuroCambia = onModoOscuroCambia
            )
        }
    }
}

@Composable
private fun DatosDelEstudiante(estudiante: Estudiante) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            FilaDeDato(Icons.Default.Person, "Nombre", estudiante.nombre)
            HorizontalDivider()
            FilaDeDato(Icons.Default.Badge, "Código", estudiante.codigo)
            HorizontalDivider()
            FilaDeDato(Icons.Default.School, "Carrera", estudiante.carrera)
            HorizontalDivider()
            FilaDeDato(Icons.Default.Email, "Correo", estudiante.correo)
        }
    }
}

@Composable
private fun InterruptorDeTema(
    modoOscuro: Boolean,
    onModoOscuroCambia: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tema oscuro",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Se aplica al instante en toda la aplicación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = modoOscuro,
                onCheckedChange = onModoOscuroCambia
            )
        }
    }
}
