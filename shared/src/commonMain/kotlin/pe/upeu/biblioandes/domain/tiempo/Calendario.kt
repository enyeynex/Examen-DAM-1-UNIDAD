package pe.upeu.biblioandes.domain.tiempo

import kotlinx.datetime.LocalDate

/**
 * El dominio necesita saber que dia es hoy, pero no de donde sale ese dato.
 * En la app lo responde el reloj del telefono; en las pruebas, una fecha fija.
 */
fun interface Calendario {
    fun hoy(): LocalDate
}
