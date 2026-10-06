package pe.upeu.biblioandes.data.repository

import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

/** Doble de la biblioteca para las pruebas: sin retardo y con datos a medida. */
class FakeBibliotecaRepository(
    libros: List<Libro> = emptyList(),
    prestamos: List<Prestamo> = emptyList()
) : BibliotecaRepository {

    val libros = libros.toMutableList()
    val prestamos = prestamos.toMutableList()

    override suspend fun obtenerEstudiante() =
        Estudiante("E-0001", "Estudiante de prueba", "Ingeniería de Sistemas", "prueba@correo.pe")

    override suspend fun obtenerCategorias() = libros.map { it.categoria }.distinct()

    override suspend fun obtenerLibros() = libros.toList()

    override suspend fun obtenerLibro(id: Int) = libros.firstOrNull { it.id == id }

    override suspend fun obtenerPrestamos() = prestamos.toList()

    override suspend fun registrarPrestamo(prestamo: Prestamo): Prestamo {
        val guardado = prestamo.copy(id = prestamos.size + 1)
        prestamos.add(guardado)
        return guardado
    }

    override suspend fun registrarDevolucion(prestamoId: Int, fechaDevolucion: String): Prestamo {
        val indice = prestamos.indexOfFirst { it.id == prestamoId }
        val devuelto = prestamos[indice].copy(estado = EstadoPrestamo.Devuelto(fechaDevolucion))
        prestamos[indice] = devuelto
        return devuelto
    }
}
