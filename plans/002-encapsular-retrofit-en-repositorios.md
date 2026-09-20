# Plan 002: Encapsular favoritos y búsquedas guardadas en repositorios

> **Instrucciones para el ejecutor**: seguí este plan paso a paso y ejecutá cada verificación. Si aparece una condición de parada, detenete e informala sin improvisar. Al terminar, actualizá el estado de este plan en `plans/README.md`, salvo que un revisor indique que mantiene el índice.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/java/com/da_grupo9/ronda/data/remote/FavoritesApi.java app/src/main/java/com/da_grupo9/ronda/data/remote/SavedSearchesApi.java app/src/main/java/com/da_grupo9/ronda/data/repository app/src/main/java/com/da_grupo9/ronda/ui/fragments/FavoritesFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/SavedSearchesFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/HomeFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/DetailFragment.java`
> Si cambió algún archivo incluido, compará el estado actual con los extractos de este plan. Una incompatibilidad es una condición de parada.

## Estado

- **Prioridad**: P1
- **Esfuerzo**: M
- **Riesgo**: MED
- **Depende de**: `plans/001-unificar-callbacks-ofertas.md`
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

Cuatro fragments ejecutan ocho llamadas Retrofit directamente. Cada uno repite la interpretación de respuestas, errores HTTP, errores de transporte y comprobaciones de ciclo de vida. Al mover favoritos y búsquedas guardadas a repositorios, la UI conservará las decisiones visuales y los repositorios concentrarán la comunicación con la API usando `RepositoryResult<T>`.

## Estado actual

- `FavoritesApi` ofrece agregar, quitar, listar y marcar favoritos como leídos.
- `SavedSearchesApi` ofrece crear, listar, marcar como leída y eliminar una búsqueda.
- `FavoritesFragment:74,118`, `SavedSearchesFragment:71,153,221`, `HomeFragment:478,810` y `DetailFragment:431` llaman `.enqueue()` desde UI.
- `PublicacionRepository.ejecutar()` y `ProfileRepository.ejecutar()` son los ejemplos de conversión desde `Call<T>` hacia `RepositoryResult<T>` mediante `ApiError.from(...)` y detección de `IOException`.
- `NetworkModule` ya provee ambas interfaces Retrofit. Los repositorios con constructor `@Inject` no requieren providers adicionales de Hilt.
- Debe conservarse el comportamiento de UI: rollback optimista de favoritos, navegación después de marcar una búsqueda, recarga después de eliminar y marcado silencioso de favoritos leídos.

## Comandos

| Propósito | Comando | Resultado esperado |
|-----------|---------|--------------------|
| Compilar | `.\gradlew.bat compileDebugJavaWithJavac --console=plain` | código 0, `BUILD SUCCESSFUL` |
| Tests locales | `.\gradlew.bat testDebugUnitTest --console=plain` | código 0, todos los tests pasan |
| Lint | `.\gradlew.bat lintDebug --console=plain` | código 0, sin errores nuevos |
| Verificar arquitectura | `rg -n "\.enqueue\(" app/src/main/java/com/da_grupo9/ronda/ui` | sin coincidencias |

## Alcance

**Archivos permitidos:**

- Crear `app/src/main/java/com/da_grupo9/ronda/data/repository/FavoritesRepository.java`.
- Crear `app/src/main/java/com/da_grupo9/ronda/data/repository/SavedSearchesRepository.java`.
- Modificar `app/src/main/java/com/da_grupo9/ronda/ui/fragments/FavoritesFragment.java`.
- Modificar `app/src/main/java/com/da_grupo9/ronda/ui/fragments/SavedSearchesFragment.java`.
- Modificar `app/src/main/java/com/da_grupo9/ronda/ui/fragments/HomeFragment.java`.
- Modificar `app/src/main/java/com/da_grupo9/ronda/ui/fragments/DetailFragment.java`.
- Modificar `plans/README.md`.

**Fuera de alcance:**

- No modificar `FavoritesApi`, `SavedSearchesApi`, `NetworkModule` ni los modelos.
- No cambiar layouts, navegación, textos visibles o reglas de favorito.
- No mover llamadas Retrofit que ya están dentro de repositorios.
- No agregar caché, reintentos, corrutinas, LiveData o ViewModels.
- No cambiar el comportamiento offline existente más allá de normalizar el mensaje de `IOException` dentro del repositorio.

## Flujo Git

- Rama sugerida: `refactor/encapsulate-fragment-network-calls`.
- Mensaje sugerido: `refactor: move fragment network calls to repositories`.
- No publicar ni abrir un PR sin instrucción del operador.

## Pasos

### 1. Crear `FavoritesRepository`

Creá una clase `@Singleton` con constructor `@Inject` que reciba `FavoritesApi`. Exponé:

```java
void getFavorites(RepositoryResult<FavoritesResponse> resultado)
void markAsRead(RepositoryResult<FavoritesReadResponse> resultado)
void setFavorite(String publicationId, boolean favorite,
                 RepositoryResult<FavoriteResponse> resultado)
```

`setFavorite` debe elegir `addFavorite` cuando `favorite` sea verdadero y `removeFavorite` cuando sea falso. Centralizá la ejecución en un método privado genérico que:

- acepte un texto fallback por operación;
- requiera cuerpo no nulo para estas respuestas;
- use `ApiError.from(response, fallback)` para errores HTTP;
- traduzca `IOException` a `"No se pudo conectar con el servidor"` y otros fallos a `"No se pudo procesar la respuesta del servidor"`.

**Verificar**: compilar y confirmar que Hilt puede construir el repositorio sin cambios en `NetworkModule`.

### 2. Crear `SavedSearchesRepository`

Creá una clase `@Singleton` con constructor `@Inject` que reciba `SavedSearchesApi`. Exponé:

```java
void create(SavedSearchRequest request, RepositoryResult<SavedSearchItem> resultado)
void getAll(RepositoryResult<SavedSearchesResponse> resultado)
void markAsRead(String id, RepositoryResult<Void> resultado)
void delete(String id, RepositoryResult<Void> resultado)
```

El ejecutor privado debe aceptar un indicador `permiteCuerpoVacio`: `false` para crear/listar y `true` para marcar/eliminar. En respuestas `2xx` con cuerpo vacío, debe llamar `resultado.onSuccess(null)` únicamente cuando el indicador sea verdadero. Conservá estos fallbacks:

- crear: `"No se pudo guardar la búsqueda"`;
- listar: `"No se pudieron cargar las búsquedas"`;
- marcar: `"No se pudo abrir la búsqueda"`;
- eliminar: `"No se pudo eliminar la búsqueda"`.

Usá la misma traducción de fallos de transporte que `FavoritesRepository`.

**Verificar**: compilar y confirmar que ambos repositorios aceptan exclusivamente `RepositoryResult<T>`.

### 3. Migrar `FavoritesFragment`

Reemplazá la inyección de `FavoritesApi` por `FavoritesRepository`. Eliminá imports de Retrofit y `ApiErrorMessage`.

- `cargarFavoritos()` debe recibir `FavoritesResponse`, mostrar sus items y ejecutar `marcarFavoritosComoLeidos()` si `unreadCount > 0`.
- `marcarFavoritosComoLeidos()` debe conservar el aviso cuando falla, sin hacer nada adicional cuando tiene éxito.
- Mantené todos los `isAdded()` antes de tocar vistas o mostrar `Toast`.

**Verificar**: `rg -n "FavoritesApi|retrofit2|\.enqueue\(" app/src/main/java/com/da_grupo9/ronda/ui/fragments/FavoritesFragment.java` no devuelve resultados.

### 4. Migrar `SavedSearchesFragment`

Reemplazá `SavedSearchesApi` por `SavedSearchesRepository`. Conservá:

- la lista renderizada por `mostrarBusquedas`;
- la navegación mediante `abrirBusquedaEnHome` después de marcar como leída;
- el toast `"Búsqueda eliminada"` y la recarga después de eliminar;
- los `isAdded()` antes de usar la vista.

Eliminá imports Retrofit y `ApiErrorMessage`.

**Verificar**: `rg -n "SavedSearchesApi|retrofit2|\.enqueue\(" app/src/main/java/com/da_grupo9/ronda/ui/fragments/SavedSearchesFragment.java` no devuelve resultados.

### 5. Migrar creación de búsquedas en `HomeFragment`

Reemplazá su inyección de `SavedSearchesApi` por `SavedSearchesRepository`. En `guardarBusqueda`, mantené la construcción de `SavedSearchRequest`, el estado enabled/disabled del botón y los mismos toasts. Sustituí únicamente el callback Retrofit por `RepositoryResult<SavedSearchItem>`.

**Verificar**: `rg -n "SavedSearchesApi|\.enqueue\(" app/src/main/java/com/da_grupo9/ronda/ui/fragments/HomeFragment.java` sólo puede seguir mostrando la operación de favoritos hasta completar el siguiente paso.

### 6. Migrar favoritos en `HomeFragment` y `DetailFragment`

Inyectá `FavoritesRepository` en ambos fragments y eliminá la inyección de `FavoritesApi`.

- En `HomeFragment.alternarFavorito`, llamá `setFavorite(id, nuevoEstado, ...)`; conservá la actualización optimista, `favoritosPendientes` y el rollback mediante `resolverFavorito`.
- En `DetailFragment`, llamá `setFavorite(id, !esFavorito, ...)`; conservá la actualización de `publicacionCargada`, el texto del botón y todos los mensajes actuales.
- Para mantener mensajes específicos de agregar/quitar, la UI puede seguir eligiendo el texto mostrado a partir de `nuevoEstado`, pero no debe analizar objetos Retrofit.

Eliminá imports `Call`, `Callback`, `Response`, APIs directas y `ApiErrorMessage` que ya no sean usados.

**Verificar**: `rg -n "\.enqueue\(" app/src/main/java/com/da_grupo9/ronda/ui` debe terminar sin coincidencias.

### 7. Ejecutar verificaciones completas

Ejecutá compilación, tests, lint y `git diff --check`. Confirmá con `git status --short` que sólo aparezcan los archivos permitidos.

## Plan de pruebas

No existe actualmente infraestructura para sustituir `Call<T>` o las APIs Retrofit en tests unitarios. No agregues una biblioteca de mocking como parte de este refactor. La cobertura de regresión se apoya en:

- compilación Hilt de las nuevas dependencias;
- tests locales existentes;
- lint;
- búsquedas estructurales que aseguran que ningún fragment conserve `.enqueue()`.

Pruebas manuales recomendadas en un entorno con backend, fuera de la puerta automatizada: listar/agregar/quitar favoritos; marcar favoritos como leídos; crear, abrir y eliminar una búsqueda guardada; simular un fallo de red y comprobar el rollback visual.

## Criterios de finalización

- [ ] Existen `FavoritesRepository` y `SavedSearchesRepository`, ambos con `@Inject` y `@Singleton`.
- [ ] Ambos repositorios usan `RepositoryResult<T>`.
- [ ] Ningún archivo bajo `ui/` importa `retrofit2` ni llama `.enqueue()`.
- [ ] Ningún fragment inyecta `FavoritesApi` o `SavedSearchesApi`.
- [ ] Se conserva el rollback optimista de favoritos.
- [ ] Crear, abrir y eliminar búsquedas conserva sus efectos de UI.
- [ ] Compilación, tests y lint terminan con código 0.
- [ ] Sólo se modificaron los archivos incluidos y `plans/README.md`.
- [ ] El estado del plan figura como `DONE`.

## Condiciones de parada

- El plan 001 no está completado o `OffersRepository` todavía declara `Result<T>`.
- Alguna API devuelve respuestas exitosas vacías donde este plan requiere cuerpo no nulo.
- Hilt no puede construir un repositorio sin modificar archivos fuera del alcance.
- Mantener los mensajes y efectos actuales exige cambiar navegación o layouts.
- Una verificación falla dos veces después de un intento razonable de corrección.

## Notas de mantenimiento

Los fragments deben decidir presentación, navegación y ciclo de vida; los repositorios deben interpretar Retrofit y exponer `RepositoryResult<T>`. El revisor debe prestar especial atención a respuestas `Void`, rollback optimista y comprobaciones `isAdded()`. La introducción futura de ViewModels o cancelación de llamadas queda deliberadamente fuera de este refactor.
