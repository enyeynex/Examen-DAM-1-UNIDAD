package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

/** Trae el catalogo completo. El filtrado lo hace [FiltrarLibrosUseCase]. */
class ObtenerCatalogoUseCase(
    private val repository: BibliotecaRepository
) {

    suspend operator fun invoke(): Result<List<Libro>> = resultadoDe {
        repository.obtenerLibros()
    }
}
