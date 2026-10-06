package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.tiempo.Calendario

/**
 * Registra la devolucion de un prestamo con la fecha de hoy. Es la forma de
 * regularizar un vencido y volver a cumplir RN-04.
 */
class DevolverPrestamoUseCase(
    private val repository: BibliotecaRepository,
    private val calendario: Calendario
) {

    suspend operator fun invoke(prestamoId: Int): Result<Prestamo> = resultadoDe {
        repository.registrarDevolucion(
            prestamoId = prestamoId,
            fechaDevolucion = calendario.hoy().toString()
        )
    }
}
