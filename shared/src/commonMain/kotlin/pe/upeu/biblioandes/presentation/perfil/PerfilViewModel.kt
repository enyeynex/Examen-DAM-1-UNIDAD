package pe.upeu.biblioandes.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.usecase.ObtenerEstudianteUseCase
import pe.upeu.biblioandes.presentation.perfil.PerfilUiState.Fase

data class PerfilUiState(
    val fase: Fase = Fase.Cargando
) {

    sealed interface Fase {

        data object Cargando : Fase

        data class Error(val mensaje: String) : Fase

        data class Contenido(val estudiante: Estudiante) : Fase
    }
}

class PerfilViewModel(
    private val obtenerEstudiante: ObtenerEstudianteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            if (_uiState.value.fase !is Fase.Contenido) {
                _uiState.update { it.copy(fase = Fase.Cargando) }
            }

            obtenerEstudiante()
                .onSuccess { estudiante ->
                    _uiState.update { it.copy(fase = Fase.Contenido(estudiante)) }
                }
                .onFailure {
                    _uiState.update { it.copy(fase = Fase.Error("No se pudo cargar tu perfil")) }
                }
        }
    }
}
