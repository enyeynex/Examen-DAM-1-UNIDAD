package pe.upeu.biblioandes.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo

/**
 * Tarjeta de un prestamo. Si se le pasa [onDevolver] y el prestamo sigue
 * pendiente, muestra el boton para registrar la devolucion.
 */
@Composable
fun PrestamoItem(
    prestamo: Prestamo,
    modifier: Modifier = Modifier,
    onDevolver: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = prestamo.libro.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = prestamo.libro.autor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Prestado el ${fechaCorta(prestamo.fechaPrestamo)} · " +
                    "Devolver hasta el ${fechaCorta(prestamo.fechaLimite)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            EtiquetaEstado(estado = prestamo.estado)

            val pendiente = prestamo.estado !is EstadoPrestamo.Devuelto
            if (onDevolver != null && pendiente) {
                OutlinedButton(
                    onClick = onDevolver,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Registrar devolución")
                }
            }
        }
    }
}
