package pe.upeu.biblioandes.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.data.repository.FakeBibliotecaRepository
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.MotivoRechazo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.tiempo.Calendario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SolicitarPrestamoUseCaseTest {

    private val calendario = Calendario { LocalDate(2026, 10, 5) }
    private val libro = Libro(1, "Kotlin en profundidad", "M. Salazar", 2023, "Programación", "Central", 3)
    private val agotado = Libro(2, "Estructuras de datos", "R. Peña", 2021, "Programación", "Central", 0)

    @Test
    fun registraElPrestamoConSieteDiasDePlazo() = runTest {

        val repositorio = FakeBibliotecaRepository(libros = listOf(libro))

        val prestamo = SolicitarPrestamoUseCase(repositorio, calendario).invoke(1).getOrThrow()

        assertEquals("2026-10-05", prestamo.fechaPrestamo)
        assertEquals("2026-10-12", prestamo.fechaLimite)
        assertEquals(EstadoPrestamo.Activo(7), prestamo.estado)
        assertEquals(1, repositorio.prestamos.size)
    }

    @Test
    fun rechazaUnLibroSinEjemplares() = runTest {

        val repositorio = FakeBibliotecaRepository(libros = listOf(agotado))

        val fallo = SolicitarPrestamoUseCase(repositorio, calendario).invoke(2).exceptionOrNull()

        assertEquals(MotivoRechazo.SIN_EJEMPLARES, assertIs<SolicitudRechazadaException>(fallo).motivo)
        assertTrue(repositorio.prestamos.isEmpty())
    }

    @Test
    fun rechazaCuandoUnPrestamoGuardadoComoActivoYaVencio() = runTest {

        // El repositorio lo guarda como Activo, pero su fecha limite ya paso:
        // el caso de uso debe recalcular el estado antes de aplicar RN-04.
        val atrasado = Prestamo(1, libro, "2026-09-20", "2026-09-27", EstadoPrestamo.Activo(2))
        val repositorio = FakeBibliotecaRepository(libros = listOf(libro), prestamos = listOf(atrasado))

        val fallo = SolicitarPrestamoUseCase(repositorio, calendario).invoke(1).exceptionOrNull()

        assertEquals(MotivoRechazo.PRESTAMO_VENCIDO, assertIs<SolicitudRechazadaException>(fallo).motivo)
    }

    @Test
    fun rechazaElCuartoPrestamoActivo() = runTest {

        val activos = (1..3).map { Prestamo(it, libro, "2026-10-03", "2026-10-10", EstadoPrestamo.Activo(5)) }
        val repositorio = FakeBibliotecaRepository(libros = listOf(libro), prestamos = activos)

        val fallo = SolicitarPrestamoUseCase(repositorio, calendario).invoke(1).exceptionOrNull()

        assertEquals(MotivoRechazo.LIMITE_DE_PRESTAMOS, assertIs<SolicitudRechazadaException>(fallo).motivo)
    }
}
