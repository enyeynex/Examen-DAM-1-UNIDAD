package pe.upeu.biblioandes.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.usecase.ObtenerEstudianteUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerProximaDevolucionUseCase
import pe.upeu.biblioandes.presentation.inicio.InicioUiState.Fase

data class InicioUiState(
    val fase: Fase = Fase.Cargando
) {

    sealed interface Fase {

        data object Cargando : Fase

        data class Error(val mensaje: String) : Fase

        /** [proximaDevolucion] es null cuando no hay nada pendiente de devolver. */
        data class Contenido(
            val estudiante: Estudiante,
            val proximaDevolucion: Prestamo?
        ) : Fase
    }
}

class InicioViewModel(
    private val obtenerEstudiante: ObtenerEstudianteUseCase,
    private val obtenerProximaDevolucion: ObtenerProximaDevolucionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            if (_uiState.value.fase !is Fase.Contenido) {
                _uiState.update { it.copy(fase = Fase.Cargando) }
            }

            // Las dos consultas corren a la vez: la espera es una sola.
            val estudiantePedido = async { obtenerEstudiante() }
            val proximaPedida = async { obtenerProximaDevolucion() }

            val estudiante = estudiantePedido.await().getOrNull()
            val proxima = proximaPedida.await()

            _uiState.update {
                if (estudiante != null && proxima.isSuccess) {
                    it.copy(fase = Fase.Contenido(estudiante, proxima.getOrNull()))
                } else {
                    it.copy(fase = Fase.Error("No se pudo cargar tu información"))
                }
            }
        }
    }
}
