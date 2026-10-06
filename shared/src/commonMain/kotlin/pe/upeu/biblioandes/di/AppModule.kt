package pe.upeu.biblioandes.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.upeu.biblioandes.data.repository.ClienteRepositorioEnMemoria
import pe.upeu.biblioandes.data.repository.ProductoRepositorioEnMemoria
import pe.upeu.biblioandes.domain.repository.ClienteRepository
import pe.upeu.biblioandes.domain.repository.ProductoRepository
import pe.upeu.biblioandes.domain.usecase.ListarClientesUseCase
import pe.upeu.biblioandes.domain.usecase.ListarProductosUseCase
import pe.upeu.biblioandes.domain.usecase.RegistrarClienteUseCase
import pe.upeu.biblioandes.domain.usecase.RegistrarProductoUseCase
import pe.upeu.biblioandes.presentation.cliente.ClienteViewModel
import pe.upeu.biblioandes.presentation.producto.ProductoViewModel


val dataModule = module {
    single<ProductoRepository> { ProductoRepositorioEnMemoria() }
    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}

val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}

val presentationModule = module {
    viewModel { ProductoViewModel(get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
}


expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
