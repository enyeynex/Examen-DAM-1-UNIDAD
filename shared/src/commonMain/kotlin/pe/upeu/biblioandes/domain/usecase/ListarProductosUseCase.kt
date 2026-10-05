package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Producto
import pe.upeu.biblioandes.domain.repository.ProductoRepository

class ListarProductosUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(): Result<List<Producto>> = resultadoDe {
        productoRepository.listar()
    }
}
