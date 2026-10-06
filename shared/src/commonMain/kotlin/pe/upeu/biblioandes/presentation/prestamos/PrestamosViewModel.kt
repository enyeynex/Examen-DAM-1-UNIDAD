package pe.upeu.biblioandes.presentation.prestamos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.usecase.DevolverPrestamoUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerPrestamosUseCase
import pe.upeu.biblioandes.presentation.prestamos.PrestamosUiState.Fase

/** Opciones del filtro por estado de la pantalla Mis prestamos. */
enum class FiltroDeEstado(val etiqueta: String) {
    TODOS("Todos"),
    ACTIVOS("Activos"),
    DEVUELTOS("Devueltos"),
    VENCIDOS("Vencidos")
}

data class PrestamosUiState(
    val fase: Fase = Fase.Cargando,
    val filtro: FiltroDeEstado = FiltroDeEstado.TODOS,
    /** Prestamo cuya devolucion se esta confirmando; null si no hay dialogo. */
    val porDevolver: Prestamo? = null
) {

    sealed interface Fase {

        data object Cargando : Fase

        data class Error(val mensaje: String) : Fase

        /** No hay prestamos que mostrar con el filtro elegido. */
        data object Vacio : Fase

        data class Contenido(val prestamos: List<Prestamo>) : Fase
    }
}

class PrestamosViewModel(
    private val obtenerPrestamos: ObtenerPrestamosUseCase,
    private val devolverPrestamo: DevolverPrestamoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamosUiState())
    val uiState: StateFlow<PrestamosUiState> = _uiState.asStateFlow()

    /** Todos los prestamos, ya ordenados por el caso de uso. Null hasta la primera carga. */
    private var prestamos: List<Prestamo>? = null

    fun cargar() {
        viewModelScope.launch {
            if (_uiState.value.fase !is Fase.Contenido) {
                _uiState.update { it.copy(fase = Fase.Cargando) }
            }
            refrescar()
        }
    }

    fun onFiltroSeleccionado(filtro: FiltroDeEstado) {
        _uiState.update { it.copy(filtro = filtro) }
        aplicarFiltro()
    }

    fun onDevolverPulsado(prestamo: Prestamo) {
        _uiState.update { it.copy(porDevolver = prestamo) }
    }

    fun onDevolucionCancelada() {
        _uiState.update { it.copy(porDevolver = null) }
    }

    fun onDevolucionConfirmada() {
        val prestamo = _uiState.value.porDevolver ?: return
        _uiState.update { it.copy(porDevolver = null) }

        viewModelScope.launch {
            devolverPrestamo(prestamo.id)
            refrescar()
        }
    }

    private suspend fun refrescar() {
        obtenerPrestamos()
            .onSuccess { lista ->
                prestamos = lista
                aplicarFiltro()
            }
            .onFailure { fallo ->
                prestamos = null
                _uiState.update {
                    it.copy(fase = Fase.Error(fallo.message ?: "No se pudieron cargar tus préstamos"))
                }
            }
    }

    /** Deja visibles solo los prestamos del estado elegido. */
    private fun aplicarFiltro() {
        val lista = prestamos ?: return

        _uiState.update { estado ->
            val visibles = lista.filter { prestamo ->
                when (estado.filtro) {
                    FiltroDeEstado.TODOS -> true
                    FiltroDeEstado.ACTIVOS -> prestamo.estado is EstadoPrestamo.Activo
                    FiltroDeEstado.DEVUELTOS -> prestamo.estado is EstadoPrestamo.Devuelto
                    FiltroDeEstado.VENCIDOS -> prestamo.estado is EstadoPrestamo.Vencido
                }
            }
            estado.copy(
                fase = if (visibles.isEmpty()) Fase.Vacio else Fase.Contenido(visibles)
            )
        }
    }
}
