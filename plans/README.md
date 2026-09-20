# Planes de implementación

Generados con la skill `improve` el 2026-09-20. Deben ejecutarse en el orden indicado. Cada ejecutor debe leer el plan completo, respetar sus condiciones de parada y actualizar el estado al terminar.

## Orden y estado

| Plan | Título | Prioridad | Esfuerzo | Depende de | Estado |
|------|--------|-----------|----------|------------|--------|
| 001 | Unificar los callbacks de ofertas con `RepositoryResult` | P1 | S | — | DONE |
| 002 | Encapsular favoritos y búsquedas guardadas en repositorios | P1 | M | 001 | DONE |
| 003 | Unificar los argumentos internos de navegación en español | P1 | M | 002 | DONE |
| 004 | Normalizar nombres internos de la capa UI | P2 | M | 003 | DONE |
| 005 | Centralizar el formato de fechas e importes | P2 | S | 004 | DONE |
| 006 | Retirar scaffolding, recursos y constructores redundantes | P2 | S | 005 | DONE |
| 007 | Podar accesores y columnas de caché sin consumidores | P3 | M | 006 | DONE |

Estados válidos: `TODO`, `IN PROGRESS`, `DONE`, `BLOCKED` o `REJECTED`.

## Dependencias

- El plan 002 depende del 001 para que todos los repositorios nuevos y existentes usen el mismo contrato `RepositoryResult<T>`.
- El plan 003 se ejecuta después de 002 para evitar editar simultáneamente los mismos fragments mientras se cambian sus dependencias.
- El plan 004 depende del 003 porque renombra variables que contienen argumentos de navegación.
- El plan 005 depende del 004 porque elimina helpers locales de `OffersFragment` que el plan anterior renombra.
- El plan 006 se deja después de los refactors funcionales para que lint mida el estado final y no genere conflictos triviales.
- El plan 007 va al final porque modifica DTO y esquema Room; debe partir de un árbol estable y verificado.
- `AuthRepository.Resultado`, `PublicacionRepository.ResultadoPagina` e `ImageUploadManager.Result` quedan fuera de estos planes: los dos primeros expresan contratos especializados y el tercero pertenece a la capa de utilidades, no a repositorios.

## Hallazgos considerados y descartados

- Reemplazar `AuthRepository.Resultado` por `RepositoryResult<Void>`: el cambio obligaría a introducir parámetros `Void` en todos los flujos de autenticación sin eliminar lógica ni mejorar la API para sus consumidores.
- Reemplazar `PublicacionRepository.ResultadoPagina`: conserva metadatos de paginación y el indicador de caché, por lo que no es equivalente a `RepositoryResult<T>`.
- Reutilizar `RepositoryResult<T>` desde `ImageUploadManager`: crearía una dependencia de una utilidad de subida hacia el paquete de repositorios.

En caso de que hagas cualquier commit hacelo en español, y no te agregues como participante. Y siempre trabaja en una branch nueva. De hacer un pull request la descripcion tambien en español.
## Verificación de los planes 005–007 (2026-09-20)

Implementados juntos en `refactor/planes-005-006-007`.

- **005:** `DateTimeFormat` centraliza el parseo y fallback. Perfiles y operaciones conservan `MEDIUM`; ofertas conserva fecha y hora `SHORT`, con llamadas directas a `MoneyFormat.amount`. Tres tests locales pasan: entradas ausentes, inválidas y una fecha ISO con offset bajo `Locale.UK`, restaurada en `finally`.
- **006:** retirados los dos colores, el drawable sin uso, la dependencia ConstraintLayout, los dos tests de plantilla y los once constructores enumerados. Los mipmaps del launcher se conservan. `package-lock.json` ya estaba ausente al comenzar. Lint ya no informa los tres `UnusedResources`.
- **007:** retirados `ApiError.getCode`, `FavoriteItem.getFavoritedAt`, ambos getters de `OfferActionResponse`, `Publicacion.getDraftStep`, `Question.getAskerId/getAskerName`, `Perfil.getUsername`, `Pagination.getPageSize` y `ReviewItem.getOperationType`. De `Operation` se retiraron `getPublicationId`, `getSellerId`, `getSellerName`, `getType`, `getBuyerId`, `getBuyerName`, `getCounterpartyAvatarUrl` y `getReviewDeadline`. Se revisaron las coincidencias por tipo cuando había métodos homónimos en otras clases; ninguno de estos accesores tenía consumidores. Los getters de operaciones usados por la UI se conservaron, al igual que todos los campos Gson salvo `draftStep`, cuyo único constructor consumidor era el fallback del mapper.
- **Caché:** eliminadas solamente las columnas `publishedAt`, `sellerRating`, `coverImage` y `localCoverImagePath`; Room procesa correctamente la versión 3. Se mantienen `fullJson`, las claves, las columnas del fallback y el control de caché. La política existente `fallbackToDestructiveMigration` recreará la caché local al actualizar desde la versión 2.
- **Limitación de pruebas del mapper:** `toModel` llama a `android.text.TextUtils.isEmpty` antes de reconstruir tanto desde `fullJson` como desde el fallback. No se agregaron tests locales de reconstrucción ni dependencias Android adicionales, según la alternativa prevista en el plan 007. Se verificaron compilación, procesamiento de Room y lint; no se probó la actualización de la base en un dispositivo.
- **Validación final:** `:app:compileDebugJavaWithJavac`, `:app:testDebugUnitTest` y `:app:lintDebug` terminaron con código 0. JUnit: 3 tests, 0 fallos; lint: 0 errores y 267 advertencias fuera del alcance de estos planes. `git diff --check` sin errores. Las búsquedas finales no encuentran los helpers duplicados, ConstraintLayout, tests de plantilla, constructores vacíos enumerados, `draftStep` ni las cuatro columnas retiradas.
