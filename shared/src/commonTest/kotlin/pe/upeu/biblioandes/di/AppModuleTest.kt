package pe.upeu.biblioandes.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import pe.upeu.biblioandes.data.repository.BibliotecaRepositoryFake
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.usecase.ObtenerCatalogoUseCase
import pe.upeu.biblioandes.domain.usecase.SolicitarPrestamoUseCase
import pe.upeu.biblioandes.presentation.catalogo.CatalogoViewModel
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroViewModel
import pe.upeu.biblioandes.presentation.inicio.InicioViewModel
import pe.upeu.biblioandes.presentation.perfil.PerfilViewModel
import pe.upeu.biblioandes.presentation.prestamos.PrestamosViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Comprueba que el grafo de Koin se ensambla sin arrancar la aplicacion: si
 * una definicion falta o cambia el constructor de una clase, falla aqui.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin = startKoin {
        modules(dataModule, domainModule, presentationModule, platformModule)
    }.koin

    @Test
    fun elRepositorioSeResuelvePorSuInterfazYEsUnico() {

        val koin = grafoCompleto()

        assertIs<BibliotecaRepositoryFake>(koin.get<BibliotecaRepository>())
        assertSame(koin.get<BibliotecaRepository>(), koin.get<BibliotecaRepository>())
    }

    @Test
    fun resuelveLosCasosDeUsoYLosViewModel() {

        val koin = grafoCompleto()

        koin.get<ObtenerCatalogoUseCase>()
        koin.get<SolicitarPrestamoUseCase>()

        koin.get<InicioViewModel>()
        koin.get<CatalogoViewModel>()
        koin.get<PrestamosViewModel>()
        koin.get<PerfilViewModel>()
        koin.get<DetalleLibroViewModel> { parametersOf(1) }
    }
}
