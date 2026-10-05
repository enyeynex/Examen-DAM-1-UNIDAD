package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.PoliticaPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.tiempo.Calendario

/**
 * Entrega los prestamos del estudiante con el estado al dia (RN-03) y
 * ordenados: primero los pendientes, del que vence antes al que vence
 * despues, y al final los ya devueltos.
 */
class ObtenerPrestamosUseCase(
    private val repository: BibliotecaRepository,
    private val calendario: Calendario
) {

    suspend operator fun invoke(): Result<List<Prestamo>> = resultadoDe {

        val hoy = calendario.hoy()

        repository.obtenerPrestamos()
            .map { PoliticaPrestamo.actualizarEstado(it, hoy) }
            .sortedWith(
                compareBy<Prestamo> { it.estado is EstadoPrestamo.Devuelto }
                    .thenBy { it.fechaLimite }
            )
    }
}
