package pe.upeu.biblioandes.domain.repository

import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.Prestamo

/**
 * Contrato de acceso a los datos de la biblioteca. El dominio y la interfaz
 * solo conocen esta interfaz: hoy la cumple una fuente simulada en memoria y
 * en la Unidad 2 la cumplira una implementacion que consuma el servicio web.
 */
interface BibliotecaRepository {

    suspend fun obtenerEstudiante(): Estudiante

    suspend fun obtenerCategorias(): List<String>

    suspend fun obtenerLibros(): List<Libro>

    /** Devuelve null cuando no existe un libro con ese id. */
    suspend fun obtenerLibro(id: Int): Libro?

    suspend fun obtenerPrestamos(): List<Prestamo>

    /** Guarda el prestamo, le asigna id y descuenta un ejemplar del libro. */
    suspend fun registrarPrestamo(prestamo: Prestamo): Prestamo

    /** Marca el prestamo como devuelto y repone el ejemplar del libro. */
    suspend fun registrarDevolucion(prestamoId: Int, fechaDevolucion: String): Prestamo
}
