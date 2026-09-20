# Plan 006: Retirar scaffolding, recursos y constructores redundantes

> **Instrucciones para el ejecutor**: limitate a elementos cuya falta de uso está confirmada por búsqueda o Android Lint. Ejecutá todas las verificaciones y actualizá el índice.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/res/values/colors.xml app/src/main/res/drawable/ic_launcher_foreground.xml app/build.gradle.kts gradle/libs.versions.toml package-lock.json app/src/test app/src/androidTest app/src/main/java/com/da_grupo9/ronda/ui/fragments`
> Detenete si alguno de los elementos listados adquirió un consumidor.

## Estado

- **Prioridad**: P2
- **Esfuerzo**: S
- **Riesgo**: LOW
- **Depende de**: `plans/005-centralizar-formatos.md`
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

El repositorio conserva recursos marcados como no usados por Lint, una dependencia sin referencias, un lockfile npm vacío, tests de plantilla y once constructores de fragments que Java genera implícitamente. Retirarlos reduce ruido y evita que el scaffolding parezca funcionalidad deliberada.

## Elementos confirmados

- `R.color.black`, `R.color.white` e `ic_launcher_foreground.xml`: reportados por `UnusedResources`.
- `androidx.constraintlayout`: declarado en Gradle sin `ConstraintLayout` en Java/XML.
- `package-lock.json`: no hay `package.json` y `packages` está vacío.
- `ExampleUnitTest` prueba `2 + 2`; `ExampleInstrumentedTest` sólo comprueba el package name.
- Constructores públicos vacíos en `DetailFragment`, `HomeFragment`, `FavoritesFragment`, `ForgotPasswordFragment`, `MisPublicacionesFragment`, `LoginFragment`, `PublicarArticuloFragment`, `ProfileFragment`, `PublicProfileFragment`, `SavedSearchesFragment` y `ResetPasswordFragment`.

## Alcance

**Permitidos:** los archivos de recursos y Gradle citados, borrar `package-lock.json`, borrar los dos tests `Example*`, modificar los once fragments enumerados y `plans/README.md`.

**Fuera de alcance:** constructores de `Publicacion`, `PublicationImage`, `PublicacionEntity` o clases con `@Inject`; dependencias de test; otros warnings de Lint; recursos launcher en carpetas mipmap.

## Pasos

### 1. Revalidar referencias y retirar recursos

Antes de borrar, ejecutá búsquedas por `R.color.black`, `R.color.white` e `ic_launcher_foreground`. Si sólo aparecen las declaraciones informadas, eliminá ambos colores y el drawable.

**Verificar**: `lintDebug` ya no reporta esos tres `UnusedResources`.

### 2. Retirar dependencia y lockfile

Eliminá `implementation(libs.constraintlayout)`, el alias `constraintlayout` y su versión del catálogo. Borrá `package-lock.json`.

**Verificar**: `rg -n 'constraintlayout' app/build.gradle.kts gradle/libs.versions.toml app/src/main` no devuelve resultados.

### 3. Retirar tests de plantilla

Borrá `ExampleUnitTest.java` y `ExampleInstrumentedTest.java`. Conservá las dependencias de JUnit/Espresso como infraestructura para `DateTimeFormatTest` y futuros tests.

**Verificar**: `rg -n 'addition_isCorrect|useAppContext' app/src` no devuelve resultados.

### 4. Retirar constructores vacíos de fragments

Eliminá sólo los once constructores enumerados. Cada clase seguirá teniendo constructor público implícito sin argumentos. No toques constructores necesarios para Gson, Room o Hilt.

**Verificar**: compilación Java exitosa y `rg -n -U 'public\s+[A-Za-z0-9_]+Fragment\(\)\s*\{\s*\}' app/src/main/java` sin coincidencias.

### 5. Ejecutar verificaciones

Ejecutá `testDebugUnitTest`, `lintDebug` y `git diff --check`. `testDebugUnitTest` debe incluir el test creado en el plan 005.

## Plan de pruebas

No agregues tests para eliminaciones estáticas. Compilación y Lint son las pruebas relevantes; el test de fechas del plan 005 asegura que la suite local no quede vacía.

## Criterios de finalización

- [ ] Los tres recursos ya no existen ni aparecen en Lint.
- [ ] No quedan referencias a ConstraintLayout ni el lockfile npm vacío.
- [ ] No quedan tests `Example*`.
- [ ] Los once fragments usan constructor implícito.
- [ ] Compilación, tests y lint terminan con código 0.
- [ ] El plan figura `DONE`.

## Condiciones de parada

- Lint o una búsqueda encuentra un consumidor real de un recurso.
- Un fragment necesita un constructor explícito por anotación o reflexión adicional.
- Quitar ConstraintLayout rompe una dependencia transitiva usada directamente.

## Notas de mantenimiento

No confundas constructores privados de clases utilitarias con redundancia: impiden instanciación y deben mantenerse.
