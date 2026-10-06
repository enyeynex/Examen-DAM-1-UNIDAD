package pe.upeu.biblioandes.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.data.repository.FakeBibliotecaRepository
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.MotivoRechazo
import pe.upeu.biblioandes.domain.model.Prestamo
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
import pe.upeu.biblioandes.presentation.catalogo.CatalogoUiState
import pe.upeu.biblioandes.presentation.catalogo.CatalogoViewModel
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroUiState
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroViewModel
import pe.upeu.biblioandes.presentation.inicio.InicioUiState
import pe.upeu.biblioandes.presentation.inicio.InicioViewModel
import pe.upeu.biblioandes.presentation.prestamos.FiltroDeEstado
import pe.upeu.biblioandes.presentation.prestamos.PrestamosUiState
import pe.upeu.biblioandes.presentation.prestamos.PrestamosViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * viewModelScope corre sobre Dispatchers.Main, que en una prueba no existe:
 * setMain lo sustituye por un dispatcher de prueba antes de cada caso.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private val calendario = Calendario { LocalDate(2026, 10, 5) }

    private val kotlin = Libro(1, "Kotlin en profundidad", "M. Salazar", 2023, "Programación", "Central", 3)
    private val calculo = Libro(3, "Cálculo aplicado", "L. Ortega", 2019, "Matemática", "Sede Norte", 2)
    private val agotado = Libro(5, "Seguridad en redes", "P. Ríos", 2024, "Redes", "Central", 0)
    private val libros = listOf(kotlin, calculo, agotado)

    private val activo = Prestamo(1, kotlin, "2026-10-03", "2026-10-10", EstadoPrestamo.Activo(5))
    private val vencido = Prestamo(2, calculo, "2026-09-10", "2026-09-17", EstadoPrestamo.Vencido(18))
    private val devuelto = Prestamo(3, calculo, "2026-08-20", "2026-08-27", EstadoPrestamo.Devuelto("2026-08-26"))

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun catalogoVm(repositorio: BibliotecaRepository) = CatalogoViewModel(
        ObtenerCatalogoUseCase(repositorio),
        ObtenerCategoriasUseCase(repositorio),
        FiltrarLibrosUseCase()
    )

    private fun detalleVm(repositorio: BibliotecaRepository, libroId: Int) = DetalleLibroViewModel(
        libroId,
        ObtenerDetalleLibroUseCase(repositorio, calendario),
        SolicitarPrestamoUseCase(repositorio, calendario)
    )

    private fun prestamosVm(repositorio: BibliotecaRepository) = PrestamosViewModel(
        ObtenerPrestamosUseCase(repositorio, calendario),
        DevolverPrestamoUseCase(repositorio, calendario)
    )

    // ---------- Catalogo

    @Test
    fun elCatalogoArrancaCargandoYLuegoMuestraLosLibros() = runTest {

        val viewModel = catalogoVm(FakeBibliotecaRepository(libros))
        assertEquals(CatalogoUiState.Fase.Cargando, viewModel.uiState.value.fase)

        viewModel.cargar()

        val fase = assertIs<CatalogoUiState.Fase.Contenido>(viewModel.uiState.value.fase)
        assertEquals(3, fase.libros.size)
        assertEquals(listOf("Programación", "Matemática", "Redes"), viewModel.uiState.value.categorias)
    }

    @Test
    fun laCategoriaYLaBusquedaFiltranLaLista() = runTest {

        val viewModel = catalogoVm(FakeBibliotecaRepository(libros))
        viewModel.cargar()

        viewModel.onCategoriaSeleccionada("Matemática")
        assertEquals(
            listOf(3),
            assertIs<CatalogoUiState.Fase.Contenido>(viewModel.uiState.value.fase).libros.map { it.id }
        )

        viewModel.onCategoriaSeleccionada(null)
        viewModel.onBusquedaCambia("RIOS")
        assertEquals(
            listOf(5),
            assertIs<CatalogoUiState.Fase.Contenido>(viewModel.uiState.value.fase).libros.map { it.id }
        )

        viewModel.onBusquedaCambia("no existe")
        assertEquals(CatalogoUiState.Fase.SinResultados, viewModel.uiState.value.fase)
    }

    @Test
    fun siElRepositorioFallaElCatalogoMuestraElError() = runTest {

        val repositorio = object : BibliotecaRepository by FakeBibliotecaRepository(libros) {
            override suspend fun obtenerLibros(): List<Libro> = error("Sin conexión")
        }
        val viewModel = catalogoVm(repositorio)

        viewModel.cargar()
        viewModel.onBusquedaCambia("kotlin")

        val fase = assertIs<CatalogoUiState.Fase.Error>(viewModel.uiState.value.fase)
        assertEquals("Sin conexión", fase.mensaje)
    }

    // ---------- Detalle

    @Test
    fun elDetallePermiteSolicitarYRegistraElPrestamo() = runTest {

        val repositorio = FakeBibliotecaRepository(libros, listOf(activo))
        val viewModel = detalleVm(repositorio, libroId = 3)
        viewModel.cargar()

        val detalle = assertIs<DetalleLibroUiState.Fase.Contenido>(viewModel.uiState.value.fase).detalle
        assertTrue(detalle.sePuedeSolicitar)

        viewModel.onSolicitarPulsado()
        assertTrue(viewModel.uiState.value.confirmando)

        viewModel.onSolicitudConfirmada()

        val estado = viewModel.uiState.value
        assertFalse(estado.confirmando)
        assertFalse(estado.solicitando)
        assertEquals(DetalleLibroUiState.Aviso.PrestamoRegistrado("2026-10-12"), estado.aviso)
        assertEquals(2, repositorio.prestamos.size)
    }

    @Test
    fun cancelarElDialogoNoRegistraNada() = runTest {

        val repositorio = FakeBibliotecaRepository(libros)
        val viewModel = detalleVm(repositorio, libroId = 1)
        viewModel.cargar()

        viewModel.onSolicitarPulsado()
        viewModel.onSolicitudCancelada()

        assertFalse(viewModel.uiState.value.confirmando)
        assertNull(viewModel.uiState.value.aviso)
        assertTrue(repositorio.prestamos.isEmpty())
    }

    @Test
    fun elDetalleIndicaElBloqueoPorPrestamoVencido() = runTest {

        val viewModel = detalleVm(FakeBibliotecaRepository(libros, listOf(vencido)), libroId = 1)
        viewModel.cargar()

        val detalle = assertIs<DetalleLibroUiState.Fase.Contenido>(viewModel.uiState.value.fase).detalle
        assertEquals(MotivoRechazo.PRESTAMO_VENCIDO, detalle.motivoDeBloqueo)
    }

    @Test
    fun unLibroInexistenteMuestraError() = runTest {

        val viewModel = detalleVm(FakeBibliotecaRepository(libros), libroId = 99)
        viewModel.cargar()

        assertIs<DetalleLibroUiState.Fase.Error>(viewModel.uiState.value.fase)
    }

    // ---------- Mis prestamos

    @Test
    fun losPrestamosSeFiltranPorEstado() = runTest {

        val viewModel = prestamosVm(FakeBibliotecaRepository(libros, listOf(devuelto, activo, vencido)))
        viewModel.cargar()

        val todos = assertIs<PrestamosUiState.Fase.Contenido>(viewModel.uiState.value.fase).prestamos
        assertEquals(listOf(2, 1, 3), todos.map { it.id })

        viewModel.onFiltroSeleccionado(FiltroDeEstado.VENCIDOS)
        assertEquals(
            listOf(2),
            assertIs<PrestamosUiState.Fase.Contenido>(viewModel.uiState.value.fase).prestamos.map { it.id }
        )

        viewModel.onFiltroSeleccionado(FiltroDeEstado.DEVUELTOS)
        assertEquals(
            listOf(3),
            assertIs<PrestamosUiState.Fase.Contenido>(viewModel.uiState.value.fase).prestamos.map { it.id }
        )
    }

    @Test
    fun sinPrestamosLaPantallaQuedaVacia() = runTest {

        val viewModel = prestamosVm(FakeBibliotecaRepository(libros))
        viewModel.cargar()

        assertEquals(PrestamosUiState.Fase.Vacio, viewModel.uiState.value.fase)
    }

    @Test
    fun devolverElVencidoLoDejaComoDevuelto() = runTest {

        val repositorio = FakeBibliotecaRepository(libros, listOf(vencido))
        val viewModel = prestamosVm(repositorio)
        viewModel.cargar()

        viewModel.onDevolverPulsado(vencido)
        assertEquals(vencido, viewModel.uiState.value.porDevolver)

        viewModel.onDevolucionConfirmada()

        assertNull(viewModel.uiState.value.porDevolver)
        assertEquals(EstadoPrestamo.Devuelto("2026-10-05"), repositorio.prestamos.single().estado)
        assertIs<EstadoPrestamo.Devuelto>(
            assertIs<PrestamosUiState.Fase.Contenido>(viewModel.uiState.value.fase).prestamos.single().estado
        )
    }

    // ---------- Inicio

    @Test
    fun elInicioDestacaElPrestamoQueVencePrimero() = runTest {

        val repositorio = FakeBibliotecaRepository(libros, listOf(devuelto, activo, vencido))
        val viewModel = InicioViewModel(
            ObtenerEstudianteUseCase(repositorio),
            ObtenerProximaDevolucionUseCase(ObtenerPrestamosUseCase(repositorio, calendario))
        )

        viewModel.cargar()

        val fase = assertIs<InicioUiState.Fase.Contenido>(viewModel.uiState.value.fase)
        assertEquals(2, fase.proximaDevolucion?.id)
        assertEquals("Estudiante de prueba", fase.estudiante.nombre)
    }

    @Test
    fun sinPendientesElInicioNoDestacaNingunPrestamo() = runTest {

        val repositorio = FakeBibliotecaRepository(libros, listOf(devuelto))
        val viewModel = InicioViewModel(
            ObtenerEstudianteUseCase(repositorio),
            ObtenerProximaDevolucionUseCase(ObtenerPrestamosUseCase(repositorio, calendario))
        )

        viewModel.cargar()

        assertNull(assertIs<InicioUiState.Fase.Contenido>(viewModel.uiState.value.fase).proximaDevolucion)
    }
}
