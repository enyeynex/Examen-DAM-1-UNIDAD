package pe.upeu.biblioandes.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.tiempo.Calendario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Comprueba que los datos semilla cumplen el minimo que pide el caso. */
class BibliotecaRepositoryFakeTest {

    private val calendario = Calendario { LocalDate(2026, 10, 5) }

    @Test
    fun laSemillaTraeDoceLibrosEnCincoCategorias() = runTest {

        val repositorio = BibliotecaRepositoryFake(calendario)
        val libros = repositorio.obtenerLibros()

        assertEquals(12, libros.size)
        assertEquals(5, repositorio.obtenerCategorias().size)
        assertEquals(repositorio.obtenerCategorias().toSet(), libros.map { it.categoria }.toSet())
        assertTrue(libros.count { it.ejemplaresDisponibles == 0 } >= 2)
    }

    @Test
    fun laSemillaTraeDosActivosDosDevueltosYUnVencido() = runTest {

        val prestamos = BibliotecaRepositoryFake(calendario).obtenerPrestamos()

        assertEquals(2, prestamos.count { it.estado is EstadoPrestamo.Activo })
        assertEquals(2, prestamos.count { it.estado is EstadoPrestamo.Devuelto })
        assertEquals(1, prestamos.count { it.estado is EstadoPrestamo.Vencido })
    }

    @Test
    fun losActivosVencenDespuesDeHoySeaCualSeaLaFecha() = runTest {

        val otroDia = Calendario { LocalDate(2027, 3, 14) }
        val activos = BibliotecaRepositoryFake(otroDia).obtenerPrestamos()
            .filter { it.estado is EstadoPrestamo.Activo }

        assertEquals(2, activos.size)
        assertTrue(activos.all { LocalDate.parse(it.fechaLimite) > LocalDate(2027, 3, 14) })
    }

    @Test
    fun prestarDescuentaUnEjemplarYDevolverLoRepone() = runTest {

        val repositorio = BibliotecaRepositoryFake(calendario)
        val libro = repositorio.obtenerLibro(1)!!
        val pendiente = repositorio.obtenerPrestamos().first { it.libro.id == 1 }

        val nuevo = repositorio.registrarPrestamo(pendiente.copy(id = 0))
        assertEquals(libro.ejemplaresDisponibles - 1, repositorio.obtenerLibro(1)!!.ejemplaresDisponibles)
        assertEquals(6, nuevo.id)

        val devuelto = repositorio.registrarDevolucion(nuevo.id, "2026-10-05")
        assertIs<EstadoPrestamo.Devuelto>(devuelto.estado)
        assertEquals(libro.ejemplaresDisponibles, repositorio.obtenerLibro(1)!!.ejemplaresDisponibles)
    }

    @Test
    fun conLaBanderaActivaElCatalogoFalla() = runTest {

        val repositorio = BibliotecaRepositoryFake(calendario, simularErrorEnCatalogo = true)

        assertFailsWith<IllegalStateException> { repositorio.obtenerLibros() }
    }
}
