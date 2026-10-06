package pe.upeu.biblioandes.data.local

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import pe.upeu.biblioandes.domain.tiempo.Calendario
import kotlin.time.Clock

/** Responde "que dia es hoy" con el reloj y la zona horaria del telefono. */
class CalendarioDelSistema : Calendario {

    override fun hoy(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
}
