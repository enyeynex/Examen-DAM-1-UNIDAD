package pe.upeu.biblioandes.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fila de chips donde solo uno esta seleccionado. Es generica: no sabe si
 * filtra categorias o estados, solo recibe las [opciones], cual esta
 * [seleccionada] y como escribir cada una. El catalogo y Mis prestamos usan
 * este mismo componente.
 */
@Composable
fun <T> ChipsDeFiltro(
    opciones: List<T>,
    seleccionada: T,
    etiqueta: (T) -> String,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) },
                label = { Text(etiqueta(opcion)) }
            )
        }
    }
}
