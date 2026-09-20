# Plan 001: Unificar los callbacks de ofertas con `RepositoryResult`

> **Instrucciones para el ejecutor**: seguí este plan paso a paso y ejecutá cada verificación. Si aparece una condición de parada, detenete e informala sin improvisar. Al terminar, actualizá el estado de este plan en `plans/README.md`, salvo que un revisor indique que mantiene el índice.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/java/com/da_grupo9/ronda/data/repository/OffersRepository.java app/src/main/java/com/da_grupo9/ronda/data/repository/RepositoryResult.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/OffersFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/CreateOfferFragment.java`
> Si cambió algún archivo incluido, compará el estado actual con los extractos de este plan. Una incompatibilidad es una condición de parada.

## Estado

- **Prioridad**: P1
- **Esfuerzo**: S
- **Riesgo**: MED
- **Depende de**: ninguno
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

El proyecto ya define `RepositoryResult<T>` como callback común, pero `OffersRepository` mantiene otro contrato genérico con las mismas operaciones. Esto obliga a los consumidores a conocer una interfaz exclusiva de ofertas y permite que el manejo de errores de los repositorios evolucione de manera divergente. Después de este cambio, ofertas utilizará el mismo contrato que publicaciones y perfil.

## Estado actual

- `data/repository/RepositoryResult.java:6-18` define:

```java
public interface RepositoryResult<T> {
    void onSuccess(T data);
    default void onSuccess(T data, boolean desdeCache) { onSuccess(data); }
    void onError(String mensaje);
    default void onError(ApiError error) { onError(error.getMessage()); }
}
```

- `data/repository/OffersRepository.java:25-29` vuelve a definir `Result<T>` con `onSuccess(T)`, `onError(String)` y `onError(ApiError)`.
- `ui/fragments/OffersFragment.java:90,195-212` consume `OffersRepository.Result` para listar y modificar ofertas.
- `ui/fragments/CreateOfferFragment.java:111-126` consume el mismo tipo para crear una oferta.
- La convención predominante es nombrar el parámetro callback `resultado`; `PublicacionRepository` es el ejemplo principal.

## Comandos

| Propósito | Comando | Resultado esperado |
|-----------|---------|--------------------|
| Compilar | `.\gradlew.bat compileDebugJavaWithJavac --console=plain` | código 0, `BUILD SUCCESSFUL` |
| Tests locales | `.\gradlew.bat testDebugUnitTest --console=plain` | código 0, todos los tests pasan |
| Lint | `.\gradlew.bat lintDebug --console=plain` | código 0, sin errores nuevos |

## Alcance

**Archivos permitidos:**

- `app/src/main/java/com/da_grupo9/ronda/data/repository/OffersRepository.java`
- `app/src/main/java/com/da_grupo9/ronda/ui/fragments/OffersFragment.java`
- `app/src/main/java/com/da_grupo9/ronda/ui/fragments/CreateOfferFragment.java`
- `plans/README.md`

**Fuera de alcance:**

- No modificar `RepositoryResult.java`; su contrato actual ya cubre ofertas.
- No modificar endpoints, modelos ni textos visibles.
- No convertir `AuthRepository.Resultado`, `PublicacionRepository.ResultadoPagina` o `ImageUploadManager.Result`.
- No cambiar los nombres en inglés de `OffersFragment`; pertenecen a otro hallazgo.

## Flujo Git

- Rama sugerida: `refactor/unify-offers-callbacks`.
- Mensaje sugerido: `refactor: unify offers repository callbacks`.
- No publicar ni abrir un PR sin instrucción del operador.

## Pasos

### 1. Cambiar el contrato de `OffersRepository`

Eliminá la interfaz anidada `OffersRepository.Result<T>`. Cambiá `getMyOffers`, `createOffer`, `respond`, `respondToCounter`, `cancel`, `requireConnection` y `execute` para aceptar `RepositoryResult<T>`. Como está en el mismo paquete, no hace falta importar la interfaz.

Renombrá los parámetros locales `result` a `resultado` y mantené exactamente las reglas actuales:

- falta de conexión: `"Se necesita conexión a internet para gestionar ofertas"`;
- respuesta fallida: `ApiError.from(...)`;
- error de transporte: el resultado de `networkError(error)`;
- respuesta exitosa: el mismo cuerpo o la lista extraída actualmente.

**Verificar**: `rg -n "interface Result|Result<" app/src/main/java/com/da_grupo9/ronda/data/repository/OffersRepository.java` no debe mostrar coincidencias; luego ejecutar la compilación, que todavía puede fallar únicamente por los dos consumidores pendientes.

### 2. Migrar los consumers de UI

En `OffersFragment` y `CreateOfferFragment`, reemplazá cada `OffersRepository.Result<...>` por `RepositoryResult<...>` e importá `com.da_grupo9.ronda.data.repository.RepositoryResult`.

No cambies el contenido de `onSuccess`, `onError(String)` ni la especialización `onError(ApiError)` de `OffersFragment.actionResult()`.

**Verificar**: `rg -n "OffersRepository\.Result" app/src/main/java` debe terminar sin coincidencias.

### 3. Ejecutar las verificaciones completas

Ejecutá compilación, tests locales y lint con los comandos de la tabla. Revisá `git diff --check` y confirmá que no existan espacios finales ni errores de whitespace.

## Plan de pruebas

No agregues tests que sólo reproduzcan el reemplazo de tipo: el comportamiento de red no cambia y el repositorio no posee actualmente una infraestructura de dobles para Retrofit. La verificación relevante es que todos los consumidores compilen y que las tareas existentes sigan pasando.

## Criterios de finalización

- [ ] `OffersRepository` no declara una interfaz `Result`.
- [ ] `rg -n "OffersRepository\.Result" app/src/main/java` no devuelve resultados.
- [ ] Los métodos públicos de ofertas aceptan `RepositoryResult<T>`.
- [ ] Compilación, tests locales y lint terminan con código 0.
- [ ] Sólo se modificaron los archivos incluidos y `plans/README.md`.
- [ ] El estado del plan figura como `DONE`.

## Condiciones de parada

- `RepositoryResult<T>` dejó de tener `onSuccess(T)` u `onError(String)`.
- Algún consumidor adicional usa `OffersRepository.Result` y requiere cambios fuera del alcance.
- La API distingue de forma necesaria una respuesta exitosa con cuerpo nulo que `RepositoryResult` no pueda representar.
- Una verificación falla dos veces después de un intento razonable de corrección.

## Notas de mantenimiento

Los repositorios nuevos deben aceptar `RepositoryResult<T>` salvo que necesiten metadatos adicionales claramente definidos. El revisor debe comprobar que este cambio sea puramente tipológico y que no altere mensajes, condiciones de conexión o manejo de respuestas.
