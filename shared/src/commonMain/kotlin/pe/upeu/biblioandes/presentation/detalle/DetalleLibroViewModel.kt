package pe.upeu.biblioandes.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.DetalleLibro
import pe.upeu.biblioandes.domain.model.MotivoRechazo
import pe.upeu.biblioandes.domain.usecase.ObtenerDetalleLibroUseCase
import pe.upeu.biblioandes.domain.usecase.SolicitarPrestamoUseCase
import pe.upeu.biblioandes.domain.usecase.SolicitudRechazadaException
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroUiState.Aviso
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroUiState.Fase

data class DetalleLibroUiState(
    val fase: Fase = Fase.Cargando,
    /** true mientras el dialogo de confirmacion esta en pantalla. */
    val confirmando: Boolean = false,
    /** true mientras se registra el prestamo. */
    val solicitando: Boolean = false,
    /** Resultado de la ultima solicitud; null si aun no se solicito nada. */
    val aviso: Aviso? = null
) {

    sealed interface Fase {

        data object Cargando : Fase

        data class Error(val mensaje: String) : Fase

        data class Contenido(val detalle: DetalleLibro) : Fase
    }

    sealed interface Aviso {

        data class PrestamoRegistrado(val fechaLimite: String) : Aviso

        data class Rechazado(val motivo: MotivoRechazo) : Aviso

        data class Fallo(val mensaje: String) : Aviso
    }
}

class DetalleLibroViewModel(
    private val libroId: Int,
    private val obtenerDetalle: ObtenerDetalleLibroUseCase,
    private val solicitarPrestamo: SolicitarPrestamoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleLibroUiState())
    val uiState: StateFlow<DetalleLibroUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            if (_uiState.value.fase !is Fase.Contenido) {
                _uiState.update { it.copy(fase = Fase.Cargando) }
            }
            refrescarDetalle()
        }
    }

    fun onSolicitarPulsado() {
        _uiState.update { it.copy(confirmando = true) }
    }

    fun onSolicitudCancelada() {
        _uiState.update { it.copy(confirmando = false) }
    }

    /** El estudiante acepto el dialogo: recien aqui se registra el prestamo. */
    fun onSolicitudConfirmada() {
        _uiState.update { it.copy(confirmando = false, solicitando = true, aviso = null) }

        viewModelScope.launch {
            val aviso = solicitarPrestamo(libroId).fold(
                onSuccess = { prestamo -> Aviso.PrestamoRegistrado(prestamo.fechaLimite) },
                onFailure = { fallo ->
                    if (fallo is SolicitudRechazadaException) {
                        Aviso.Rechazado(fallo.motivo)
                    } else {
                        Aviso.Fallo(fallo.message ?: "No se pudo registrar el préstamo")
                    }
                }
            )

            // Tras la solicitud cambian los ejemplares y quiza ya no se pueda
            // pedir otro: se vuelve a consultar el detalle.
            refrescarDetalle()
            _uiState.update { it.copy(solicitando = false, aviso = aviso) }
        }
    }

    private suspend fun refrescarDetalle() {
        obtenerDetalle(libroId)
            .onSuccess { detalle ->
                _uiState.update { it.copy(fase = Fase.Contenido(detalle)) }
            }
            .onFailure { fallo ->
                _uiState.update {
                    it.copy(fase = Fase.Error(fallo.message ?: "No se pudo cargar el libro"))
                }
            }
    }
}
