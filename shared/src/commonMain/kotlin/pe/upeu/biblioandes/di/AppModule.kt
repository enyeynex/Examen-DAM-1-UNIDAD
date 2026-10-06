package pe.upeu.biblioandes.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.upeu.biblioandes.data.local.CalendarioDelSistema
import pe.upeu.biblioandes.data.repository.BibliotecaRepositoryFake
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.tiempo.Calendario
import pe.upeu.biblioandes.domain.usecase.DevolverPrestamoUseCase
import pe.upeu.biblioandes.domain.usecase.FiltrarLibrosUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerCatalogoUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerCategoriasUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerDetalleLibroUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerEstudianteUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerPrestamosUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerProximaDevolucionUseCase
import pe.upeu.biblioandes.domain.usecase.SolicitarPrestamoUseCase
import pe.upeu.biblioandes.presentation.catalogo.CatalogoViewModel
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroViewModel
import pe.upeu.biblioandes.presentation.inicio.InicioViewModel
import pe.upeu.biblioandes.presentation.perfil.PerfilViewModel
import pe.upeu.biblioandes.presentation.prestamos.PrestamosViewModel

/**
 * Capa de datos. Cuando exista el servicio web, la unica linea que cambia en
 * toda la aplicacion es la del repositorio: se registra la implementacion que
 * consuma la API en lugar de [BibliotecaRepositoryFake].
 *
 * single: una sola instancia para toda la app, porque guarda las listas en
 * memoria y todas las pantallas deben ver los mismos datos.
 */
val dataModule = module {
    single<Calendario> { CalendarioDelSistema() }
    single<BibliotecaRepository> { BibliotecaRepositoryFake(calendario = get()) }
}

/** Casos de uso. factory: no guardan estado, se crea uno nuevo cada vez que se pide. */
val domainModule = module {
    factory { ObtenerEstudianteUseCase(get()) }
    factory { ObtenerCategoriasUseCase(get()) }
    factory { ObtenerCatalogoUseCase(get()) }
    factory { FiltrarLibrosUseCase() }
    factory { ObtenerDetalleLibroUseCase(get(), get()) }
    factory { SolicitarPrestamoUseCase(get(), get()) }
    factory { ObtenerPrestamosUseCase(get(), get()) }
    factory { ObtenerProximaDevolucionUseCase(get()) }
    factory { DevolverPrestamoUseCase(get(), get()) }
}

/** ViewModels. Koin los entrega atados al ciclo de vida de cada pantalla. */
val presentationModule = module {
    viewModel { InicioViewModel(get(), get()) }
    viewModel { CatalogoViewModel(get(), get(), get()) }
    viewModel { parametros ->
        // El id del libro no esta en el grafo: lo pasa la pantalla al pedir el ViewModel.
        DetalleLibroViewModel(
            libroId = parametros.get(),
            obtenerDetalle = get(),
            solicitarPrestamo = get()
        )
    }
    viewModel { PrestamosViewModel(get(), get()) }
    viewModel { PerfilViewModel(get()) }
}

/** Dependencias propias de cada plataforma; cada una declara su version (actual). */
expect val platformModule: Module

/**
 * Arranca Koin con los modulos comunes. Android lo llama desde
 * MainApplication e iOS desde iOSApp.swift, cada uno con su configuracion.
 */
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
