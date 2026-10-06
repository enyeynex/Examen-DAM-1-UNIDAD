package pe.upeu.biblioandes.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.data.repository.FakeBibliotecaRepository
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.tiempo.Calendario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CatalogoYPrestamosUseCaseTest {

    private val calendario = Calendario { LocalDate(2026, 10, 5) }

    private val libros = listOf(
        Libro(1, "Kotlin en profundidad", "M. Salazar", 2023, "Programación", "Central", 3),
        Libro(3, "Cálculo aplicado", "L. Ortega", 2019, "Matemática", "Sede Norte", 2),
        Libro(4, "Redes de computadoras", "A. Medina", 2022, "Redes", "Sede Sur", 4)
    )

    @Test
    fun laBusquedaNoDistingueMayusculasNiTildes() {
        val encontrados = FiltrarLibrosUseCase().invoke(libros, categoria = null, busqueda = "CALCULO")
        assertEquals(listOf(3), encontrados.map { it.id })
    }

    @Test
    fun laBusquedaTambienMiraElAutor() {
        val encontrados = FiltrarLibrosUseCase().invoke(libros, categoria = null, busqueda = "medina")
        assertEquals(listOf(4), encontrados.map { it.id })
    }

    @Test
    fun laCategoriaYLaBusquedaSeCombinan() {
        val encontrados = FiltrarLibrosUseCase().invoke(libros, categoria = "Redes", busqueda = "kotlin")
        assertEquals(emptyList(), encontrados)
    }

    @Test
    fun losPrestamosSalenConEstadoAlDiaYPendientesPrimero() = runTest {

        val libro = libros.first()
        val repositorio = FakeBibliotecaRepository(
            libros = libros,
            prestamos = listOf(
                Prestamo(1, libro, "2026-08-20", "2026-08-27", EstadoPrestamo.Devuelto("2026-08-26")),
                Prestamo(2, libro, "2026-10-03", "2026-10-10", EstadoPrestamo.Activo(1)),
                Prestamo(3, libro, "2026-09-17", "2026-09-24", EstadoPrestamo.Activo(1))
            )
        )

        val prestamos = ObtenerPrestamosUseCase(repositorio, calendario).invoke().getOrThrow()

        assertEquals(listOf(3, 2, 1), prestamos.map { it.id })
        assertEquals(EstadoPrestamo.Vencido(11), prestamos[0].estado)
        assertEquals(EstadoPrestamo.Activo(5), prestamos[1].estado)
        assertIs<EstadoPrestamo.Devuelto>(prestamos[2].estado)
    }

    @Test
    fun laProximaDevolucionEsElPendienteQueVencePrimero() = runTest {

        val libro = libros.first()
        val repositorio = FakeBibliotecaRepository(
            libros = libros,
            prestamos = listOf(
                Prestamo(1, libro, "2026-08-20", "2026-08-27", EstadoPrestamo.Devuelto("2026-08-26")),
                Prestamo(2, libro, "2026-10-03", "2026-10-10", EstadoPrestamo.Activo(5)),
                Prestamo(3, libro, "2026-10-01", "2026-10-08", EstadoPrestamo.Activo(3))
            )
        )

        val proxima = ObtenerProximaDevolucionUseCase(
            ObtenerPrestamosUseCase(repositorio, calendario)
        ).invoke().getOrThrow()

        assertEquals(3, proxima?.id)
    }
}
