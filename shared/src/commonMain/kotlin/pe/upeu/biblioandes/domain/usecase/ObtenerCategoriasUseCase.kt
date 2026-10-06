package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

class ObtenerCategoriasUseCase(
    private val repository: BibliotecaRepository
) {

    suspend operator fun invoke(): Result<List<String>> = resultadoDe {
        repository.obtenerCategorias()
    }
}
