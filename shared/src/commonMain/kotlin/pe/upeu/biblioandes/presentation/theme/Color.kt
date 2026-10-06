package pe.upeu.biblioandes.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Paleta propia de BiblioAndes. Reemplaza el morado que Material 3 trae por
 * defecto: terracota como color principal (el adobe y la arcilla de los
 * Andes), verde azulado como acento y neutros calidos, como papel de libro.
 */

// Terracota: botones, elementos seleccionados y titulos destacados.
private val Terracota = Color(0xFF9A4521)
private val TerracotaClaro = Color(0xFFFFDBCE)
private val TerracotaProfundo = Color(0xFF370E00)
private val TerracotaNocturno = Color(0xFFFFB599)

// Arcilla: color secundario, para chips y elementos de apoyo.
private val Arcilla = Color(0xFF77574B)
private val ArcillaClaro = Color(0xFFF5DED5)
private val ArcillaNocturno = Color(0xFFE7BDB0)

// Verde azulado de laguna: prestamos activos y disponibilidad.
private val Laguna = Color(0xFF2A6767)
private val LagunaClaro = Color(0xFFB0ECEB)
private val LagunaNocturno = Color(0xFF94D0CF)

val EsquemaClaro = lightColorScheme(
    primary = Terracota,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = TerracotaClaro,
    onPrimaryContainer = TerracotaProfundo,
    secondary = Arcilla,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = ArcillaClaro,
    onSecondaryContainer = Color(0xFF2C160D),
    tertiary = Laguna,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = LagunaClaro,
    onTertiaryContainer = Color(0xFF002020),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231A16),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231A16),
    surfaceVariant = Color(0xFFF5DED6),
    onSurfaceVariant = Color(0xFF53433D),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFEF1EC),
    surfaceContainer = Color(0xFFF8EBE6),
    surfaceContainerHigh = Color(0xFFF2E5E0),
    surfaceContainerHighest = Color(0xFFECE0DB),
    outline = Color(0xFF85736C),
    outlineVariant = Color(0xFFD8C2BA)
)

val EsquemaOscuro = darkColorScheme(
    primary = TerracotaNocturno,
    onPrimary = Color(0xFF5A1C00),
    primaryContainer = Color(0xFF7B2F0C),
    onPrimaryContainer = TerracotaClaro,
    secondary = ArcillaNocturno,
    onSecondary = Color(0xFF442A20),
    secondaryContainer = Color(0xFF5D4035),
    onSecondaryContainer = ArcillaClaro,
    tertiary = LagunaNocturno,
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF084F4F),
    onTertiaryContainer = LagunaClaro,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A110E),
    onBackground = Color(0xFFF1DFD9),
    surface = Color(0xFF1A110E),
    onSurface = Color(0xFFF1DFD9),
    surfaceVariant = Color(0xFF53433D),
    onSurfaceVariant = Color(0xFFD8C2BA),
    surfaceContainerLowest = Color(0xFF140C09),
    surfaceContainerLow = Color(0xFF231A16),
    surfaceContainer = Color(0xFF271E1A),
    surfaceContainerHigh = Color(0xFF322824),
    surfaceContainerHighest = Color(0xFF3D322E),
    outline = Color(0xFFA08D85),
    outlineVariant = Color(0xFF53433D)
)
