package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Libro

/**
 * Filtra un catalogo ya cargado por categoria y por texto de busqueda.
 * No es suspend ni toca el repositorio: trabaja sobre la lista en memoria,
 * asi cada tecla o chip responde al instante sin volver a cargar.
 */
class FiltrarLibrosUseCase {

    operator fun invoke(
        libros: List<Libro>,
        categoria: String?,
        busqueda: String
    ): List<Libro> {

        val texto = busqueda.trim().sinTildes()

        return libros
            .filter { categoria == null || it.categoria == categoria }
            .filter {
                texto.isEmpty() ||
                    it.titulo.sinTildes().contains(texto) ||
                    it.autor.sinTildes().contains(texto)
            }
    }

    /** Deja el texto en minusculas y sin tildes para comparar sin distinguirlas. */
    private fun String.sinTildes(): String {
        return lowercase().map { letra -> TILDES[letra] ?: letra }.joinToString("")
    }

    private companion object {
        val TILDES = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u'
        )
    }
}
