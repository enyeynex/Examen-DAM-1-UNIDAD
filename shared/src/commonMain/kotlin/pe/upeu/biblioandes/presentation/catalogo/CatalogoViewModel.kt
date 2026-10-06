package pe.upeu.biblioandes.presentation.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.usecase.FiltrarLibrosUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerCatalogoUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerCategoriasUseCase
import pe.upeu.biblioandes.presentation.catalogo.CatalogoUiState.Fase

class CatalogoViewModel(
    private val obtenerCatalogo: ObtenerCatalogoUseCase,
    private val obtenerCategorias: ObtenerCategoriasUseCase,
    private val filtrarLibros: FiltrarLibrosUseCase
) : ViewModel() {

    // El estado se modifica solo desde aqui (_uiState es privado); la pantalla
    // recibe la version de solo lectura y no puede cambiarla por su cuenta.
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    /**
     * Catalogo completo, tal como llego del repositorio. Los filtros trabajan
     * sobre el. Es null mientras no haya una carga correcta.
     */
    private var catalogo: List<Libro>? = null

    private var carga: Job? = null

    /** La pantalla lo llama cada vez que se muestra, para tener datos al dia. */
    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {

            // El indicador de carga solo aparece si todavia no hay libros en
            // pantalla; si ya los hay, se actualizan sin parpadeo.
            if (_uiState.value.fase !is Fase.Contenido) {
                _uiState.update { it.copy(fase = Fase.Cargando) }
            }

            // Las dos consultas corren a la vez: la espera es una sola.
            val categoriasPedidas = async { obtenerCategorias() }
            val catalogoPedido = async { obtenerCatalogo() }

            val categorias = categoriasPedidas.await().getOrDefault(emptyList())

            catalogoPedido.await()
                .onSuccess { libros ->
                    catalogo = libros
                    _uiState.update { it.copy(categorias = categorias) }
                    aplicarFiltros()
                }
                .onFailure { fallo ->
                    catalogo = null
                    _uiState.update {
                        it.copy(fase = Fase.Error(fallo.message ?: "No se pudo cargar el catálogo"))
                    }
                }
        }
    }

    fun onBusquedaCambia(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        aplicarFiltros()
    }

    fun onCategoriaSeleccionada(categoria: String?) {
        _uiState.update { it.copy(categoriaSeleccionada = categoria) }
        aplicarFiltros()
    }

    /** Vuelve a calcular la lista visible con la categoria y la busqueda actuales. */
    private fun aplicarFiltros() {
        // Sin catalogo cargado (cargando o con error) no hay nada que filtrar.
        val libros = catalogo ?: return

        _uiState.update { estado ->
            val visibles = filtrarLibros(
                libros = libros,
                categoria = estado.categoriaSeleccionada,
                busqueda = estado.busqueda
            )
            estado.copy(
                fase = if (visibles.isEmpty()) Fase.SinResultados else Fase.Contenido(visibles)
            )
        }
    }
}
