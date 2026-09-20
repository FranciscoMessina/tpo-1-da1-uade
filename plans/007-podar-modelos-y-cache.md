# Plan 007: Podar accesores y columnas de caché sin consumidores

> **Instrucciones para el ejecutor**: este plan toca modelos deserializados y Room. Revalidá cada referencia antes de eliminar y detenete si aparece uso por reflexión configurada explícitamente, tests externos o código nuevo.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/java/com/da_grupo9/ronda/data/model app/src/main/java/com/da_grupo9/ronda/data/local app/src/main/java/com/da_grupo9/ronda/di/DatabaseModule.java app/src/main/java/com/da_grupo9/ronda/util/ApiError.java`
> Cualquier consumidor nuevo de los símbolos objetivo es una condición de parada para ese símbolo, no una autorización para modificar al consumidor.

## Estado

- **Prioridad**: P3
- **Esfuerzo**: M
- **Riesgo**: MED
- **Depende de**: `plans/006-limpiar-scaffolding-y-recursos.md`
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

Varios accesores públicos no tienen consumidores y algunas columnas Room se escriben pero nunca participan en consultas ni reconstrucción del modelo. Mantener superficie y datos duplicados sugiere capacidades inexistentes y aumenta el costo de cambios de esquema. Gson accede a campos privados directamente, pero la eliminación debe limitarse a accesores no usados y preservar campos que formen parte del payload.

## Estado actual

Accesores sin llamadas fuera de su declaración:

- `ApiError.getCode`.
- `FavoriteItem.getFavoritedAt`.
- `OfferActionResponse.getOffer` y, tras revalidar, `getMessage` si continúa sin consumidor.
- `Operation.getBuyerId`, `getBuyerName`, `getCounterpartyAvatarUrl`, `getReviewDeadline` y los demás getters que la búsqueda global confirme sin llamadas.
- `Publicacion.getDraftStep`, `Question.getAskerId`, `Question.getAskerName`.
- `Perfil.getUsername`, `Pagination.getPageSize`, `ReviewItem.getOperationType`.

Columnas duplicadas de caché: `publishedAt`, `sellerRating`, `coverImage` y `localCoverImagePath` se guardan en `PublicacionEntity`, pero `PublicacionMapper.toModel` reconstruye desde `fullJson` y su fallback no lee esas columnas. `status`, `cachedAt`, `lastConsultedAt`, `hasDetail`, claves y campos usados por el fallback sí tienen consumidores y deben conservarse.

Room está en versión 2 y `DatabaseModule` usa `fallbackToDestructiveMigration`, por lo que retirar columnas requiere incrementar la versión y recreará solamente la caché local.

## Alcance

**Permitidos:** `ApiError.java`; los modelos que contienen accesores confirmados; `PublicacionEntity.java`; `PublicacionMapper.java`; `RondaDatabase.java`; tests nuevos bajo `app/src/test`; `plans/README.md`.

**Fuera de alcance:** nombres de campos JSON todavía recibidos, campos de `Operation` aunque se retire su getter, DAO queries, sesión, migración persistente de la caché y cambios de endpoints.

## Pasos

### 1. Generar y revisar inventario definitivo

Para cada getter candidato, buscá su nombre completo en `app/src/main`, `app/src/test` y `app/src/androidTest`. Eliminá sólo métodos cuya única coincidencia sea la declaración. No elimines el campo privado correspondiente si Gson puede poblarlo o si otro método lo usa internamente.

`ApiError.code` debe conservarse porque `normalizedCode()` lo usa; sólo puede retirarse `getCode()`.

**Verificar**: compilación Java exitosa después de retirar los accesores.

### 2. Evaluar y retirar `draftStep`

Si `draftStep` continúa apareciendo únicamente como campo, parámetro del constructor fallback y getter, eliminá los tres y ajustá `PublicacionMapper.toModel` para invocar el constructor sin ese parámetro. Conservá un constructor sin argumentos para Gson.

Si el backend o una pantalla nueva lo consume, dejalo intacto y documentá el motivo en el estado del plan.

**Verificar**: `rg -n 'draftStep|getDraftStep' app/src` no devuelve resultados o el estado del plan registra explícitamente por qué se conservó.

### 3. Retirar columnas de caché sin lectura

Revalidá que `publishedAt`, `sellerRating`, `coverImage` y `localCoverImagePath` de `PublicacionEntity` no aparezcan en queries ni en `toModel`. Eliminá campos y getters/setters; eliminá las llamadas `set*` correspondientes en `PublicacionMapper.toEntity`.

No confundas `Publicacion.coverImage` o `Publicacion.publishedAt` con las columnas de la entidad: los campos del DTO se conservan.

Incrementá `RondaDatabase` de versión 2 a 3. No agregues una migración: la base contiene caché y el proyecto ya declaró `fallbackToDestructiveMigration`.

**Verificar**: `rg -n '\b(publishedAt|sellerRating|localCoverImagePath|coverImage)\b' app/src/main/java/com/da_grupo9/ronda/data/local` no debe devolver coincidencias relativas a la entidad eliminada; usos de imágenes dentro del mapper sobre `Publicacion` pueden permanecer.

### 4. Probar la reconstrucción de caché

Agregá tests unitarios de `PublicacionMapper` sólo si pueden ejecutarse sin Android runtime. Deben cubrir serialización/deserialización por `fullJson` y el fallback con los campos denormalizados conservados. Si `android.text.TextUtils` impide un test local, no agregues Robolectric ni nuevas dependencias: registrá esa limitación y usá compilación/lint como puerta automatizada.

### 5. Ejecutar verificaciones

Ejecutá compilación, `testDebugUnitTest`, `lintDebug` y `git diff --check`. Confirmá que Room procese el nuevo esquema sin errores de anotación.

## Criterios de finalización

- [ ] Sólo se retiraron getters con cero consumidores.
- [ ] Los campos Gson necesarios permanecen aunque su getter no se use.
- [ ] `draftStep` fue retirado o su conservación quedó justificada.
- [ ] Las cuatro columnas sin lectura fueron retiradas y Room está en versión 3.
- [ ] Se conservaron columnas usadas por DAO, fallback y control de caché.
- [ ] Compilación, tests y lint terminan con código 0.
- [ ] El plan figura `DONE` o documenta cada candidato conservado.

## Condiciones de parada

- Un getter tiene un consumidor nuevo o forma parte de una interfaz externa.
- Una columna objetivo aparece en una query Room o en reconstrucción efectiva.
- La base contiene datos no regenerables, contradiciendo su uso como caché.
- Room exige una migración que no pueda resolverse con la política destructiva ya declarada.

## Notas de mantenimiento

No uses ausencia de getters como evidencia para borrar campos JSON. Gson deserializa campos privados. Las columnas Room, en cambio, forman un esquema local y su eliminación siempre exige incrementar la versión.
