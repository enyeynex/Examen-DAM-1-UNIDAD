# BiblioAndes

Aplicación móvil para la biblioteca de un instituto tecnológico: el estudiante consulta el catálogo, solicita préstamos y controla sus fechas de devolución.

Producto de la Unidad 1 del curso Desarrollo de Aplicaciones Móviles (UPeU, 2026-2). Está hecha con Kotlin Multiplatform y Compose Multiplatform: una sola base de código que se ejecuta en Android e iOS.

**Integrante:** Adiel Lujan

## Cómo ejecutarla

**Android**

1. Abre la carpeta del proyecto en Android Studio y espera a que termine la sincronización de Gradle.
2. Elige la configuración `androidApp` y un emulador o dispositivo con Android 8.0 (API 26) o superior.
3. Pulsa Run.

Desde la terminal: `./gradlew :androidApp:assembleDebug`

**iOS** (requiere una Mac con Xcode)

1. Abre `iosApp/iosApp.xcodeproj` en Xcode.
2. Elige un simulador de iPhone y pulsa Run.

**Pruebas unitarias**

`./gradlew :shared:testAndroidHostTest`

## Qué hace

| Pantalla | Qué muestra |
|---|---|
| Inicio | Saludo al estudiante, tarjeta con el préstamo que vence primero y accesos rápidos |
| Catálogo | Lista de libros, filtro por categoría con chips y búsqueda por título o autor sin distinguir mayúsculas ni tildes |
| Detalle del libro | Datos del libro y la acción «Solicitar préstamo» con diálogo de confirmación |
| Mis préstamos | Préstamos ordenados por fecha de devolución, filtro por estado y registro de devolución |
| Perfil y ajustes | Datos del estudiante e interruptor de tema claro/oscuro |

La barra inferior tiene tres destinos (Inicio, Catálogo, Préstamos). Detalle y Perfil se abren encima y regresan con la flecha o con el botón atrás del sistema.

## Estructura de paquetes

Todo el código compartido está en `shared/src/commonMain/kotlin/pe/upeu/biblioandes/`:

```
├── App.kt                    Raíz de la interfaz; aquí vive el estado del tema
├── domain/                   Reglas del negocio. No depende de ninguna otra capa
│   ├── model/                Libro, Prestamo, EstadoPrestamo, Estudiante, PoliticaPrestamo
│   ├── repository/           BibliotecaRepository (solo la interfaz)
│   ├── tiempo/               Calendario: de dónde sale la fecha de hoy
│   └── usecase/              Un caso de uso por acción del estudiante
├── data/                     De dónde salen los datos
│   ├── local/                DatosSimulados (datos semilla) y CalendarioDelSistema
│   └── repository/           BibliotecaRepositoryFake (implementación en memoria)
├── presentation/             Lo que se ve
│   ├── inicio/               InicioViewModel, InicioScreen
│   ├── catalogo/             CatalogoViewModel, CatalogoUiState, CatalogoScreen
│   ├── detalle/              DetalleLibroViewModel, DetalleLibroScreen
│   ├── prestamos/            PrestamosViewModel, PrestamosScreen
│   ├── perfil/               PerfilViewModel, PerfilScreen
│   ├── components/           Composables reutilizables
│   ├── navigation/           AppNavHost, Destinos
│   └── theme/                Color, Type, BiblioAndesTheme
└── di/                       AppModule (módulos de Koin)
```

`androidApp/` e `iosApp/` solo contienen el punto de entrada de cada plataforma.

## Decisiones de arquitectura

**Clean + MVVM en tres capas.** Las dependencias apuntan hacia el dominio: `presentation` y `data` conocen a `domain`, pero `domain` no conoce a ninguna de las dos. Una pantalla nunca toca la fuente de datos: habla con su ViewModel, el ViewModel con los casos de uso y los casos de uso con la interfaz del repositorio.

**Las reglas de negocio están en el dominio.** Las cuatro viven en `domain/model/PoliticaPrestamo.kt`:

| Regla | Dónde está |
|---|---|
| RN-01: máximo tres préstamos activos | `alcanzoElLimite` |
| RN-02: no se presta un libro sin ejemplares | `tieneEjemplares` |
| RN-03: el préstamo dura siete días y luego se muestra Vencido | `fechaLimite`, `estadoPendiente` |
| RN-04: un préstamo vencido bloquea solicitudes nuevas | `tieneVencidos` |

`SolicitarPrestamoUseCase` las aplica antes de registrar un préstamo. Ningún composable las evalúa: la pantalla de detalle recibe del dominio si el libro se puede solicitar y, cuando no, el motivo.

**El estado del préstamo es una sealed class.** Cada estado lleva un dato distinto: `Activo` los días restantes, `Devuelto` la fecha de devolución y `Vencido` los días de atraso. Con un enum o un texto, todos los préstamos cargarían las tres propiedades y dos quedarían vacías. Además, el `when` sobre una sealed class obliga a cubrir los tres casos.

**Los ViewModel exponen `StateFlow<UiState>`.** El `MutableStateFlow` es privado, así que solo el ViewModel cambia el estado; la pantalla recibe la versión de solo lectura. Cada `UiState` describe la pantalla completa, con fases excluyentes (cargando, error, vacío, contenido).

**Pantallas sin estado propio.** Cada pantalla tiene dos funciones: una `Route` que obtiene el ViewModel y una `Screen` que solo recibe el estado y funciones para avisar de cada acción (state hoisting).

**Inyección de dependencias con Koin.** Los módulos se declaran una vez en `di/AppModule.kt` (commonMain). Android los arranca en `MainApplication` e iOS en `iOSApp.swift`.

**Tema Material 3 propio.** Paleta terracota y verde azulado en `Color.kt`, tipografía con serifas en los títulos en `Type.kt`. El modo claro/oscuro se guarda en `App.kt`, en la raíz del árbol de composición, por eso el cambio se aplica al instante en toda la aplicación.

**Navegación.** `Destinos.kt` define las rutas como constantes y `AppNavHost.kt` contiene el `Scaffold` con la barra inferior y el `NavHost`.

## Datos simulados

La aplicación no usa red ni base de datos. `BibliotecaRepositoryFake` guarda todo en memoria y arranca con `DatosSimulados`: un estudiante, cinco categorías, doce libros y cinco préstamos (dos activos, dos devueltos y uno vencido).

- Las fechas de los préstamos se calculan a partir del día en que se ejecuta la app, para que los activos queden siempre en el futuro.
- Cada lectura espera 800 ms con `delay`, dentro de una corrutina, para que se vea el estado de carga.
- Para ver el estado de error del catálogo, cambia `simularErrorEnCatalogo` a `true` en `BibliotecaRepositoryFake`.

Como el préstamo vencido bloquea las solicitudes (RN-04), para probar una solicitud primero hay que registrar su devolución en Mis préstamos.

## Qué cambia cuando exista el servicio web

El objetivo de esta organización es que pasar de datos simulados a datos reales sea una sustitución localizada:

1. **Crear** `data/repository/BibliotecaRepositoryApi.kt`, que implemente `BibliotecaRepository` consumiendo la API.
2. **Crear** en `data/` los modelos de la respuesta del servicio y su conversión a los modelos de dominio.
3. **Modificar una línea** en `di/AppModule.kt`: registrar la nueva implementación en lugar de `BibliotecaRepositoryFake`.
4. **Modificar** `gradle/libs.versions.toml` y `shared/build.gradle.kts` para agregar el cliente HTTP.

No se toca ningún archivo de `domain` ni de `presentation`.
