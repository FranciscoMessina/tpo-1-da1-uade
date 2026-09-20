# Plan 005: Centralizar el formato de fechas e importes

> **Instrucciones para el ejecutor**: mantené exactamente los formatos visibles actuales. Ejecutá cada verificación y actualizá el índice. Detenete ante una condición de parada.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/java/com/da_grupo9/ronda/util app/src/main/java/com/da_grupo9/ronda/ui/fragments/OffersFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/ProfileFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/PublicProfileFragment.java`
> El plan 004 habrá renombrado métodos de `OffersFragment`; seguí el símbolo equivalente si el cuerpo de formato coincide.

## Estado

- **Prioridad**: P2
- **Esfuerzo**: S
- **Riesgo**: LOW
- **Depende de**: `plans/004-normalizar-nombres-ui.md`
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

El mismo parseo de fechas ISO y fallback se repite en `OperationFormat`, `ProfileFragment` y `PublicProfileFragment`. `OffersFragment` mantiene una variante corta y un wrapper de una línea para importes. Una utilidad de fechas con métodos explícitos evita deriva sin mezclar presentación de operaciones con presentación general.

## Estado actual

- `OperationFormat.date`: `OffsetDateTime.parse`, formato localizado `MEDIUM`, devuelve `"sin datos"` o el valor original ante error.
- `ProfileFragment.formatearFecha` y `PublicProfileFragment.formatearFecha` repiten exactamente esa lógica.
- `OffersFragment.formatDate` usa `ofLocalizedDateTime(FormatStyle.SHORT)` con el mismo fallback.
- `OffersFragment.money` sólo delega en `MoneyFormat.amount`.

## Alcance

**Permitidos:** crear `util/DateTimeFormat.java`; modificar `util/OperationFormat.java`, `OffersFragment.java`, `ProfileFragment.java`, `PublicProfileFragment.java` y `plans/README.md`.

**Fuera de alcance:** recursos de strings, formato monetario interno de `MoneyFormat`, zonas horarias, textos agregados alrededor de las fechas y cualquier otro fragment.

## Pasos

### 1. Crear `DateTimeFormat`

Creá una clase utilitaria final con constructor privado y dos métodos estáticos:

```java
public static String mediumDate(String iso)
public static String shortDateTime(String iso)
```

Ambos deben devolver `"sin datos"` para null/vacío, parsear con `OffsetDateTime.parse`, usar `DateTimeFormatter` localizado con el estilo actual y devolver el input sin modificar si ocurre `RuntimeException`.

**Verificar**: compilar.

### 2. Migrar consumidores

- `OperationFormat.date` debe delegar en `DateTimeFormat.mediumDate`; conservar el método para no ampliar el refactor de operaciones.
- Reemplazá `formatearFecha` en ambos perfiles por llamadas a `DateTimeFormat.mediumDate` y eliminá imports de `java.time` que queden sin uso.
- Reemplazá el helper de fecha de ofertas por `DateTimeFormat.shortDateTime`.
- Reemplazá `money(value)` por `MoneyFormat.amount(value)` y eliminá el wrapper.

**Verificar**: `rg -n 'formatearFecha|private String (formatDate|money)' app/src/main/java` no devuelve resultados.

### 3. Verificar salida y calidad

Ejecutá compilación, tests, lint y `git diff --check`. Comprobá que `MEDIUM` siga aplicándose a perfiles/operaciones y `SHORT` a ofertas.

## Plan de pruebas

Agregá `app/src/test/java/com/da_grupo9/ronda/util/DateTimeFormatTest.java` con casos para null, vacío, ISO válido y texto inválido. Para el caso válido, fijá temporalmente `Locale` dentro de `try/finally` para evitar dependencia del equipo. Verificá ambos estilos sin asumir zona horaria del dispositivo.

## Criterios de finalización

- [ ] Existe una sola implementación del parseo/fallback de fechas.
- [ ] Perfiles y operaciones conservan estilo `MEDIUM`; ofertas conserva `SHORT` con hora.
- [ ] No existe el wrapper local `money`.
- [ ] Los nuevos tests y todas las tareas Gradle terminan con código 0.
- [ ] El plan figura `DONE`.

## Condiciones de parada

- Los textos visibles cambian bajo la misma `Locale`.
- El plan 004 eliminó o alteró la semántica de los helpers objetivo.
- Los tests requieren Android runtime en lugar de JUnit local.

## Notas de mantenimiento

Nuevas fechas ISO visibles deben pasar por `DateTimeFormat`. `OperationFormat` conserva únicamente composición específica de operaciones.
