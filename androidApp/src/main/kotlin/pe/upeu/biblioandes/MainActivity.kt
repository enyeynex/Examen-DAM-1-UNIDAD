package pe.upeu.biblioandes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(onModoOscuroAplicado = ::ajustarBarrasDelSistema)
        }
    }

    /**
     * La hora y los iconos de la barra de estado los dibuja Android, no
     * Compose. Con el tema oscuro de la app deben ser claros, y al reves,
     * para que no se pierdan contra el fondo.
     */
    private fun ajustarBarrasDelSistema(modoOscuro: Boolean) {
        val estilo = if (modoOscuro) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
    }
}
