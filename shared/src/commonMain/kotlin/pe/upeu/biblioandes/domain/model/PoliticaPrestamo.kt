package pe.upeu.biblioandes.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/**
 * Las cuatro reglas de negocio de BiblioAndes viven aqui, en el dominio.
 * Ninguna pantalla las repite: los casos de uso las consultan y la interfaz
 * solo muestra el resultado.
 */
object PoliticaPrestamo {

    /** RN-01: maximo de prestamos en estado Activo al mismo tiempo. */
    const val MAXIMO_PRESTAMOS_ACTIVOS = 3

    /** RN-03: duracion de todo prestamo, en dias. */
    const val DIAS_DE_PRESTAMO = 7

    /** RN-01: el estudiante ya no puede sumar otro prestamo activo. */
    fun alcanzoElLimite(prestamos: List<Prestamo>): Boolean {
        return prestamos.count { it.estado is EstadoPrestamo.Activo } >= MAXIMO_PRESTAMOS_ACTIVOS
    }

    /** RN-02: solo se presta un libro que tiene ejemplares. */
    fun tieneEjemplares(libro: Libro): Boolean {
        return libro.ejemplaresDisponibles > 0
    }

    /** RN-03: la fecha limite es siempre siete dias despues del prestamo. */
    fun fechaLimite(fechaPrestamo: LocalDate): LocalDate {
        return fechaPrestamo.plus(DIAS_DE_PRESTAMO, DateTimeUnit.DAY)
    }

    /**
     * RN-03: un prestamo sin devolver esta Activo mientras no pase su fecha
     * limite; desde el dia siguiente se muestra como Vencido.
     */
    fun estadoPendiente(fechaLimite: LocalDate, hoy: LocalDate): EstadoPrestamo {
        val diasQueFaltan = hoy.daysUntil(fechaLimite)
        return if (diasQueFaltan >= 0) {
            EstadoPrestamo.Activo(diasRestantes = diasQueFaltan)
        } else {
            EstadoPrestamo.Vencido(diasDeAtraso = -diasQueFaltan)
        }
    }

    /** RN-03: recalcula el estado con la fecha de hoy. Un Devuelto no cambia. */
    fun actualizarEstado(prestamo: Prestamo, hoy: LocalDate): Prestamo {
        if (prestamo.estado is EstadoPrestamo.Devuelto) return prestamo
        val limite = LocalDate.parse(prestamo.fechaLimite)
        return prestamo.copy(estado = estadoPendiente(limite, hoy))
    }

    /** RN-04: un prestamo vencido bloquea cualquier solicitud nueva. */
    fun tieneVencidos(prestamos: List<Prestamo>): Boolean {
        return prestamos.any { it.estado is EstadoPrestamo.Vencido }
    }

    /**
     * Reune RN-01, RN-02 y RN-04. Devuelve null cuando el prestamo procede
     * y, si no, el primer motivo que lo impide.
     */
    fun motivoDeRechazo(libro: Libro, prestamos: List<Prestamo>): MotivoRechazo? {
        return when {
            !tieneEjemplares(libro) -> MotivoRechazo.SIN_EJEMPLARES
            tieneVencidos(prestamos) -> MotivoRechazo.PRESTAMO_VENCIDO
            alcanzoElLimite(prestamos) -> MotivoRechazo.LIMITE_DE_PRESTAMOS
            else -> null
        }
    }
}
