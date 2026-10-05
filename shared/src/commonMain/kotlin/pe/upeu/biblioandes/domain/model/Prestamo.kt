package pe.upeu.biblioandes.domain.model

/** Las fechas viajan como texto ISO (aaaa-mm-dd), igual que las entregara la API. */
data class Prestamo(
    val id: Int,
    val libro: Libro,
    val fechaPrestamo: String,
    val fechaLimite: String,
    val estado: EstadoPrestamo
)
