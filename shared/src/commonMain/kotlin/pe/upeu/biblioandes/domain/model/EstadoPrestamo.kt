package pe.upeu.biblioandes.domain.model

/**
 * Cada estado lleva un dato distinto: los dias que faltan, la fecha en que se
 * devolvio o los dias de atraso. Con un enum o un String habria que cargar
 * las tres propiedades en todos los prestamos y dejar dos de ellas vacias.
 */
sealed class EstadoPrestamo {
    data class Activo(val diasRestantes: Int) : EstadoPrestamo()
    data class Devuelto(val fechaDevolucion: String) : EstadoPrestamo()
    data class Vencido(val diasDeAtraso: Int) : EstadoPrestamo()
}
