package pe.upeu.biblioandes.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import pe.upeu.biblioandes.domain.model.DetalleLibro
import pe.upeu.biblioandes.domain.model.PoliticaPrestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.tiempo.Calendario

/**
 * Trae un libro y averigua si el estudiante puede solicitarlo, consultando
 * las reglas en [PoliticaPrestamo]. La pantalla recibe la respuesta hecha.
 */
class ObtenerDetalleLibroUseCase(
    private val repository: BibliotecaRepository,
    private val calendario: Calendario
) {

    suspend operator fun invoke(libroId: Int): Result<DetalleLibro> = resultadoDe {
        coroutineScope {

            // Las dos consultas no dependen una de la otra: se piden a la vez.
            val libroPedido = async { repository.obtenerLibro(libroId) }
            val prestamosPedidos = async { repository.obtenerPrestamos() }

            val libro = libroPedido.await()
                ?: error("No se encontró el libro solicitado")

            val hoy = calendario.hoy()
            val prestamos = prestamosPedidos.await()
                .map { PoliticaPrestamo.actualizarEstado(it, hoy) }

            DetalleLibro(
                libro = libro,
                motivoDeBloqueo = PoliticaPrestamo.motivoDeRechazo(libro, prestamos)
            )
        }
    }
}
