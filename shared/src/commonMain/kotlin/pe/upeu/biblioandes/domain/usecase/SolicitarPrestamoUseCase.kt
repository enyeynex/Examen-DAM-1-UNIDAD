package pe.upeu.biblioandes.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import pe.upeu.biblioandes.domain.model.MotivoRechazo
import pe.upeu.biblioandes.domain.model.PoliticaPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.tiempo.Calendario

/** El dominio nego el prestamo; [motivo] dice cual regla lo impidio. */
class SolicitudRechazadaException(
    val motivo: MotivoRechazo
) : IllegalStateException("La solicitud de préstamo no cumple las reglas de la biblioteca")

/**
 * Registra un prestamo nuevo solo si pasa las reglas RN-01, RN-02 y RN-04,
 * y le pone la fecha limite que manda RN-03.
 */
class SolicitarPrestamoUseCase(
    private val repository: BibliotecaRepository,
    private val calendario: Calendario
) {

    suspend operator fun invoke(libroId: Int): Result<Prestamo> = resultadoDe {
        coroutineScope {

            // Las dos consultas no dependen una de la otra: se piden a la vez.
            val libroPedido = async { repository.obtenerLibro(libroId) }
            val prestamosPedidos = async { repository.obtenerPrestamos() }

            val libro = libroPedido.await()
                ?: error("No se encontró el libro solicitado")

            val hoy = calendario.hoy()
            val prestamos = prestamosPedidos.await()
                .map { PoliticaPrestamo.actualizarEstado(it, hoy) }

            val motivo = PoliticaPrestamo.motivoDeRechazo(libro, prestamos)
            if (motivo != null) throw SolicitudRechazadaException(motivo)

            val limite = PoliticaPrestamo.fechaLimite(hoy)

            repository.registrarPrestamo(
                Prestamo(
                    id = 0,
                    libro = libro,
                    fechaPrestamo = hoy.toString(),
                    fechaLimite = limite.toString(),
                    estado = PoliticaPrestamo.estadoPendiente(limite, hoy)
                )
            )
        }
    }
}
