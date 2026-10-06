package pe.upeu.biblioandes.data.local

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.PoliticaPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo

/**
 * Fuente de datos simulada: todo vive en memoria, sin red ni base de datos.
 * Es el unico archivo que conoce los datos semilla; cuando exista el servicio
 * web, estos datos llegaran desde la API y este archivo deja de usarse.
 */
object DatosSimulados {

    val estudiante = Estudiante(
        "E-2291", "Diego Huamán Ccama",
        "Ingeniería de Sistemas", "diego.huaman@correo.pe"
    )

    val categorias = listOf(
        "Programación", "Matemática", "Redes", "Gestión", "Literatura"
    )

    val libros = listOf(
        Libro(1, "Kotlin en profundidad", "M. Salazar", 2023, "Programación", "Central", 3),
        Libro(2, "Estructuras de datos", "R. Peña", 2021, "Programación", "Central", 0),
        Libro(3, "Cálculo aplicado", "L. Ortega", 2019, "Matemática", "Sede Norte", 2),
        Libro(4, "Redes de computadoras", "A. Medina", 2022, "Redes", "Sede Sur", 4),
        Libro(5, "Seguridad en redes", "P. Ríos", 2024, "Redes", "Central", 0),
        Libro(6, "Gestión de proyectos", "S. Delgado", 2021, "Gestión", "Sede Norte", 2),
        Libro(7, "Álgebra lineal", "C. Quispe", 2020, "Matemática", "Central", 5),
        Libro(8, "Arquitectura limpia en móviles", "J. Torres", 2024, "Programación", "Sede Sur", 1),
        Libro(9, "Liderazgo de equipos ágiles", "V. Paredes", 2023, "Gestión", "Central", 3),
        Libro(10, "Los ríos profundos", "J. M. Arguedas", 1958, "Literatura", "Central", 2),
        Libro(11, "Tradiciones peruanas", "R. Palma", 1872, "Literatura", "Sede Norte", 1),
        Libro(12, "El mundo es ancho y ajeno", "C. Alegría", 1941, "Literatura", "Sede Sur", 3)
    )

    /**
     * Los cinco prestamos del estudiante: dos Activos, dos Devueltos y uno
     * Vencido. Las fechas se calculan a partir de [hoy] y no estan escritas a
     * mano: asi los Activos quedan siempre en el futuro, sea cual sea el dia
     * en que se ejecute la aplicacion.
     */
    fun prestamos(hoy: LocalDate): List<Prestamo> = listOf(
        pendiente(1, libros[0], hoy, diasParaElLimite = 5),
        pendiente(2, libros[3], hoy, diasParaElLimite = 6),
        devuelto(3, libros[2], hoy, limiteHaceDias = 39),
        devuelto(4, libros[1], hoy, limiteHaceDias = 54),
        pendiente(5, libros[5], hoy, diasParaElLimite = -18)
    )

    /** Prestamo sin devolver: Activo si el limite aun no pasa, Vencido si ya paso. */
    private fun pendiente(id: Int, libro: Libro, hoy: LocalDate, diasParaElLimite: Int): Prestamo {
        val limite = hoy.plus(diasParaElLimite, DateTimeUnit.DAY)
        val inicio = limite.minus(PoliticaPrestamo.DIAS_DE_PRESTAMO, DateTimeUnit.DAY)
        return Prestamo(
            id, libro, inicio.toString(), limite.toString(),
            PoliticaPrestamo.estadoPendiente(limite, hoy)
        )
    }

    /** Prestamo que se devolvio un dia antes de su fecha limite. */
    private fun devuelto(id: Int, libro: Libro, hoy: LocalDate, limiteHaceDias: Int): Prestamo {
        val limite = hoy.minus(limiteHaceDias, DateTimeUnit.DAY)
        val inicio = limite.minus(PoliticaPrestamo.DIAS_DE_PRESTAMO, DateTimeUnit.DAY)
        val devolucion = limite.minus(1, DateTimeUnit.DAY)
        return Prestamo(
            id, libro, inicio.toString(), limite.toString(),
            EstadoPrestamo.Devuelto(devolucion.toString())
        )
    }
}
