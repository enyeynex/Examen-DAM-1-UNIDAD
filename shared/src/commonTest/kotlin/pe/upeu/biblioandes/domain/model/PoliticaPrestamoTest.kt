package pe.upeu.biblioandes.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PoliticaPrestamoTest {

    private val hoy = LocalDate(2026, 10, 5)
    private val libro = Libro(1, "Kotlin en profundidad", "M. Salazar", 2023, "Programación", "Central", 3)

    private fun prestamo(id: Int, estado: EstadoPrestamo, limite: String = "2026-10-10") =
        Prestamo(id, libro, "2026-10-03", limite, estado)

    @Test
    fun laFechaLimiteEsSieteDiasDespues() {
        assertEquals(LocalDate(2026, 10, 12), PoliticaPrestamo.fechaLimite(hoy))
    }

    @Test
    fun elDiaDelLimiteElPrestamoSigueActivo() {
        assertEquals(EstadoPrestamo.Activo(0), PoliticaPrestamo.estadoPendiente(hoy, hoy))
    }

    @Test
    fun pasadoElLimiteElPrestamoQuedaVencido() {
        val limite = LocalDate(2026, 10, 2)
        assertEquals(EstadoPrestamo.Vencido(3), PoliticaPrestamo.estadoPendiente(limite, hoy))
    }

    @Test
    fun unDevueltoNoCambiaAunqueHayaPasadoLaFecha() {
        val devuelto = prestamo(1, EstadoPrestamo.Devuelto("2026-08-26"), limite = "2026-08-27")
        assertEquals(devuelto, PoliticaPrestamo.actualizarEstado(devuelto, hoy))
    }

    @Test
    fun conTresActivosSeRechazaPorLimite() {
        val prestamos = (1..3).map { prestamo(it, EstadoPrestamo.Activo(5)) }
        assertEquals(MotivoRechazo.LIMITE_DE_PRESTAMOS, PoliticaPrestamo.motivoDeRechazo(libro, prestamos))
    }

    @Test
    fun sinEjemplaresSeRechaza() {
        val agotado = libro.copy(ejemplaresDisponibles = 0)
        assertEquals(MotivoRechazo.SIN_EJEMPLARES, PoliticaPrestamo.motivoDeRechazo(agotado, emptyList()))
    }

    @Test
    fun conUnVencidoSeRechaza() {
        val prestamos = listOf(prestamo(1, EstadoPrestamo.Vencido(4)))
        assertEquals(MotivoRechazo.PRESTAMO_VENCIDO, PoliticaPrestamo.motivoDeRechazo(libro, prestamos))
    }

    @Test
    fun conDosActivosYEjemplaresElPrestamoProcede() {
        val prestamos = (1..2).map { prestamo(it, EstadoPrestamo.Activo(5)) }
        assertNull(PoliticaPrestamo.motivoDeRechazo(libro, prestamos))
    }
}
