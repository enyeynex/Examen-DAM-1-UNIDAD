package pe.upeu.biblioandes.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val Base = Typography()

/**
 * Tipografia de BiblioAndes: los titulos usan una letra con serifas, como la
 * portada de un libro; el texto corrido conserva la letra del sistema para
 * que las listas se lean rapido.
 */
val TipografiaBiblioAndes = Typography(
    headlineMedium = Base.headlineMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = Base.headlineSmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = Base.titleLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = Base.titleMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold
    )
)
