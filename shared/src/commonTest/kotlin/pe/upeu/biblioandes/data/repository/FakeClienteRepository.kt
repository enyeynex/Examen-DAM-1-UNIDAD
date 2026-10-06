package pe.upeu.biblioandes.data.repository

import pe.upeu.biblioandes.domain.model.Cliente
import pe.upeu.biblioandes.domain.repository.ClienteRepository

/** Doble de la cartera de clientes, gemelo de [FakeProductoRepository]. */
class FakeClienteRepository(
    private val clientes: MutableList<Cliente> = mutableListOf()
) : ClienteRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null

    private var siguienteId = 1L

    override suspend fun registrar(cliente: Cliente): Cliente {

        fallaAlRegistrar?.let { throw it }

        val guardado = cliente.copy(id = siguienteId++)
        clientes.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Cliente> {

        fallaAlListar?.let { throw it }

        return clientes.toList()
    }
}
