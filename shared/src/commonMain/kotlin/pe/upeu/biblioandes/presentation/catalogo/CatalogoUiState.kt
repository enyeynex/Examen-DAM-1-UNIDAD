package pe.upeu.biblioandes.presentation.catalogo

import pe.upeu.biblioandes.domain.model.Libro

/**
 * Todo lo que la pantalla del catalogo necesita para dibujarse. No es el
 * modelo de dominio: el dominio describe la biblioteca (un Libro), el UiState
 * describe la pantalla (que fase muestra, que escribio el estudiante, que
 * chip esta marcado).
 */
data class CatalogoUiState(
    val fase: Fase = Fase.Cargando,
    val categorias: List<String> = emptyList(),
    /** null significa "Todas". */
    val categoriaSeleccionada: String? = null,
    val busqueda: String = ""
) {

    /** Fases excluyentes: la pantalla esta en una sola a la vez. */
    sealed interface Fase {

        data object Cargando : Fase

        data class Error(val mensaje: String) : Fase

        /** La carga termino, pero ningun libro pasa los filtros. */
        data object SinResultados : Fase

        data class Contenido(val libros: List<Libro>) : Fase
    }
}
