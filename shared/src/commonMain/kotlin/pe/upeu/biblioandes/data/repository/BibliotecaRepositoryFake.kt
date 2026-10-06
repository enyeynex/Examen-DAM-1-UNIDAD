package pe.upeu.biblioandes.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.upeu.biblioandes.data.local.DatosSimulados
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.tiempo.Calendario

/**
 * Implementacion simulada del repositorio: guarda todo en listas en memoria
 * y arranca con los datos de [DatosSimulados].
 *
 * Es la unica clase que habra que reemplazar cuando exista el servicio web:
 * una implementacion nueva de [BibliotecaRepository] que consuma la API y un
 * cambio de una linea en el modulo de Koin. Ni los casos de uso ni las
 * pantallas se enteran.
 */
class BibliotecaRepositoryFake(
    calendario: Calendario,
    /**
     * Bandera para la evaluacion: en true, el catalogo falla al cargar y la
     * pantalla muestra su estado de error.
     */
    var simularErrorEnCatalogo: Boolean = false
) : BibliotecaRepository {

    // Koin registra este repositorio como single: todas las pantallas comparten
    // las mismas listas. El Mutex evita que dos corrutinas las modifiquen a la vez.
    private val candado = Mutex()
    private val libros = DatosSimulados.libros.toMutableList()
    private val prestamos = DatosSimulados.prestamos(calendario.hoy()).toMutableList()

    override suspend fun obtenerEstudiante(): Estudiante {
        delay(RETARDO_DE_CARGA_MS)
        return DatosSimulados.estudiante
    }

    override suspend fun obtenerCategorias(): List<String> {
        delay(RETARDO_DE_CARGA_MS)
        return DatosSimulados.categorias
    }

    override suspend fun obtenerLibros(): List<Libro> {
        delay(RETARDO_DE_CARGA_MS)
        if (simularErrorEnCatalogo) {
            error("No se pudo cargar el catálogo. Inténtalo de nuevo.")
        }
        return candado.withLock { libros.toList() }
    }

    override suspend fun obtenerLibro(id: Int): Libro? {
        delay(RETARDO_DE_CARGA_MS)
        return candado.withLock { libros.firstOrNull { it.id == id } }
    }

    override suspend fun obtenerPrestamos(): List<Prestamo> {
        delay(RETARDO_DE_CARGA_MS)
        return candado.withLock { prestamos.toList() }
    }

    override suspend fun registrarPrestamo(prestamo: Prestamo): Prestamo {
        return candado.withLock {
            val libro = cambiarEjemplares(prestamo.libro.id, cambio = -1)
            val guardado = prestamo.copy(
                id = (prestamos.maxOfOrNull { it.id } ?: 0) + 1,
                libro = libro
            )
            prestamos.add(guardado)
            guardado
        }
    }

    override suspend fun registrarDevolucion(prestamoId: Int, fechaDevolucion: String): Prestamo {
        return candado.withLock {
            val indice = prestamos.indexOfFirst { it.id == prestamoId }
            check(indice >= 0) { "No se encontró el préstamo" }

            val libro = cambiarEjemplares(prestamos[indice].libro.id, cambio = +1)
            val devuelto = prestamos[indice].copy(
                libro = libro,
                estado = EstadoPrestamo.Devuelto(fechaDevolucion)
            )
            prestamos[indice] = devuelto
            devuelto
        }
    }

    /** Suma o resta ejemplares disponibles a un libro y devuelve el libro actualizado. */
    private fun cambiarEjemplares(libroId: Int, cambio: Int): Libro {
        val indice = libros.indexOfFirst { it.id == libroId }
        check(indice >= 0) { "No se encontró el libro" }

        val actualizado = libros[indice].copy(
            ejemplaresDisponibles = libros[indice].ejemplaresDisponibles + cambio
        )
        libros[indice] = actualizado
        return actualizado
    }

    private companion object {
        /** Retardo que simula la respuesta del servicio web. */
        const val RETARDO_DE_CARGA_MS = 800L
    }
}
