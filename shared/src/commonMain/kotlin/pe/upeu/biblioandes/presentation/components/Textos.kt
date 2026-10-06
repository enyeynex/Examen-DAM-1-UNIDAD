package pe.upeu.biblioandes.presentation.components

import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.MotivoRechazo
import pe.upeu.biblioandes.domain.model.PoliticaPrestamo

/*
 * Textos que la interfaz muestra a partir de datos del dominio. El dominio
 * entrega datos (una fecha ISO, un estado, un motivo) y aqui se decide con
 * que palabras se le cuentan al estudiante.
 */

private val MESES = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

/** Convierte "2026-10-12" en "12 oct 2026". */
fun fechaCorta(fechaIso: String): String {
    val fecha = LocalDate.parse(fechaIso)
    return "${fecha.day} ${MESES[fecha.month.ordinal]} ${fecha.year}"
}

/**
 * El when recorre los tres estados de la sealed class y el compilador obliga
 * a cubrirlos todos: si se agregara un cuarto estado, esto dejaria de compilar.
 */
fun EstadoPrestamo.descripcion(): String = when (this) {
    is EstadoPrestamo.Activo -> when (diasRestantes) {
        0 -> "Vence hoy"
        1 -> "Falta 1 día"
        else -> "Faltan $diasRestantes días"
    }
    is EstadoPrestamo.Devuelto -> "Devuelto el ${fechaCorta(fechaDevolucion)}"
    is EstadoPrestamo.Vencido -> when (diasDeAtraso) {
        1 -> "Vencido hace 1 día"
        else -> "Vencido hace $diasDeAtraso días"
    }
}

fun ejemplaresDisponibles(cantidad: Int): String = when (cantidad) {
    0 -> "Sin ejemplares"
    1 -> "1 disponible"
    else -> "$cantidad disponibles"
}

fun MotivoRechazo.explicacion(): String = when (this) {
    MotivoRechazo.LIMITE_DE_PRESTAMOS ->
        "Ya tienes ${PoliticaPrestamo.MAXIMO_PRESTAMOS_ACTIVOS} préstamos activos. " +
            "Devuelve uno para solicitar otro libro."
    MotivoRechazo.SIN_EJEMPLARES ->
        "No quedan ejemplares disponibles de este libro."
    MotivoRechazo.PRESTAMO_VENCIDO ->
        "Tienes un préstamo vencido. Regularízalo en Mis préstamos para solicitar un libro nuevo."
}
