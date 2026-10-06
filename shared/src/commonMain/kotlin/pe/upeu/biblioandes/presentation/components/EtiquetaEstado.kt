package pe.upeu.biblioandes.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.domain.model.EstadoPrestamo

/** Pastilla de color con un texto corto. No sabe que significa el texto. */
@Composable
fun Etiqueta(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = fondo,
        contentColor = colorTexto
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Etiqueta del estado de un prestamo: cada estado tiene su color. */
@Composable
fun EtiquetaEstado(
    estado: EstadoPrestamo,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

    val (fondo, colorTexto) = when (estado) {
        is EstadoPrestamo.Activo -> colores.tertiaryContainer to colores.onTertiaryContainer
        is EstadoPrestamo.Devuelto -> colores.surfaceContainerHighest to colores.onSurfaceVariant
        is EstadoPrestamo.Vencido -> colores.errorContainer to colores.onErrorContainer
    }

    Etiqueta(
        texto = estado.descripcion(),
        fondo = fondo,
        colorTexto = colorTexto,
        modifier = modifier
    )
}

/** Etiqueta de disponibilidad de un libro. */
@Composable
fun EtiquetaDisponibilidad(
    ejemplares: Int,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

    Etiqueta(
        texto = ejemplaresDisponibles(ejemplares),
        fondo = if (ejemplares > 0) colores.tertiaryContainer else colores.surfaceContainerHighest,
        colorTexto = if (ejemplares > 0) colores.onTertiaryContainer else colores.onSurfaceVariant,
        modifier = modifier
    )
}
