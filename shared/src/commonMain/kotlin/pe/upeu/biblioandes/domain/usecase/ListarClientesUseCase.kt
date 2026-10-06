package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Cliente
import pe.upeu.biblioandes.domain.repository.ClienteRepository


class ListarClientesUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(): Result<List<Cliente>> = resultadoDe {
        clienteRepository.listar()
    }
}
