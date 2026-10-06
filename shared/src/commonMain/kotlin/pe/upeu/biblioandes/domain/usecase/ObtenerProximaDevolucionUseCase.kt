package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo

/**
 * El prestamo que la pantalla de inicio destaca: el pendiente cuya fecha
 * limite llega primero. Devuelve null si no hay nada por devolver.
 */
class ObtenerProximaDevolucionUseCase(
    private val obtenerPrestamos: ObtenerPrestamosUseCase
) {

    suspend operator fun invoke(): Result<Prestamo?> {
        return obtenerPrestamos().map { prestamos ->
            prestamos.firstOrNull { it.estado !is EstadoPrestamo.Devuelto }
        }
    }
}
