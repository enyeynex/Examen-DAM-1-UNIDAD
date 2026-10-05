package pe.upeu.biblioandes.domain.model

/**
 * Lo que la pantalla de detalle necesita saber de un libro: sus datos y,
 * si no se puede solicitar, la regla que lo impide.
 */
data class DetalleLibro(
    val libro: Libro,
    val motivoDeBloqueo: MotivoRechazo?
) {
    val sePuedeSolicitar: Boolean
        get() = motivoDeBloqueo == null
}
