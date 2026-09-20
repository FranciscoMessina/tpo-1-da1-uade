# Plan 004: Normalizar nombres internos de la capa UI

> **Instrucciones para el ejecutor**: aplicá sólo renombres mecánicos y no cambies comportamiento. Ejecutá todas las verificaciones y actualizá el índice. Ante una condición de parada, informá sin ampliar el alcance.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/java/com/da_grupo9/ronda/ui/fragments/OffersFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/CreateOfferFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/QuestionFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/SavedItemsFragment.java app/src/main/java/com/da_grupo9/ronda/ui/fragments/OperationDetailFragment.java app/src/main/java/com/da_grupo9/ronda/ui/components/RatingBottomSheet.java`
> Son esperables cambios de los planes 001–003. Detenete si desaparecieron o cambiaron de responsabilidad los símbolos objetivo.

## Estado

- **Prioridad**: P2
- **Esfuerzo**: M
- **Riesgo**: MED
- **Depende de**: `plans/003-unificar-argumentos-navegacion.md`
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

La UI usa mayoritariamente nombres de acciones y dominio en español, pero varios fragments recientes usan inglés completo o mezclan ambos idiomas. Esto hace que búsquedas y revisiones requieran conocer dos vocabularios para la misma operación. El cambio debe establecer una regla limitada y explícita, sin tocar nombres impuestos por Android o por el backend.

## Convención objetivo

- Acciones privadas y sustantivos de dominio propios de la app: español (`cargarOfertas`, `rolSeleccionado`, `contraparte`).
- Overrides de Android y callbacks de librerías: conservar nombres obligatorios (`onCreateView`, `onSuccess`, `onError`).
- Prefijos de vistas ya generalizados: conservar `button`, `text`, `container`, `spinner`, `input`, `progress`.
- DTO, endpoints, estados y campos JSON: conservar inglés para mantener el contrato de API.
- Nombres de tipos existentes como `Repository`, `Fragment` y `BottomSheet`: conservarlos.

## Alcance

**Permitidos:** `OffersFragment.java`, `CreateOfferFragment.java`, `QuestionFragment.java`, `SavedItemsFragment.java`, `OperationDetailFragment.java`, `RatingBottomSheet.java` y `plans/README.md`.

**Fuera de alcance:** modelos, Retrofit, recursos XML/IDs, métodos públicos, overrides, textos visibles y cambios de lógica. No renombrar todos los prefijos de vistas del proyecto.

## Renombres mínimos requeridos

En `OffersFragment`:

- `offers` → `ofertas`; `selectedRole` → `rolSeleccionado`; `loading` → `cargando`; `firstResume` → `primerResume`.
- `loadOffers` → `cargarOfertas`; `renderOffers` → `renderizarOfertas`; `createCard` → `crearTarjeta`; `showCounterDialog` → `mostrarDialogoContraoferta`.
- `sellerAction` → `accionVendedor`; `counterAction` → `accionContraoferta`; `cancelOffer` → `cancelarOferta`; `actionResult` → `resultadoAccion`.
- `setLoading` → `establecerCarga`; `addAction` → `agregarAccion`; `optionalText` → `textoOpcional`; `statusLabel` → `etiquetaEstado`; `expiryText` → `textoVencimiento`.

En los demás archivos:

- `CreateOfferFragment.publicationId` → `publicacionId`; su `setLoading` → `establecerCarga`.
- `QuestionFragment.setLoading` y `RatingBottomSheet.setLoading` → `establecerCarga`.
- `SavedItemsFragment.showTab` → `mostrarPestana`.
- `OperationDetailFragment`: `operation` → `operacion`, `counterpartyName` → `nombreContraparte`, `counterpartyReputation` → `reputacionContraparte`. Conservá los prefijos `text` y `button` de las vistas.

`formatDate` y `money` de `OffersFragment` serán retirados por el plan 005; no es obligatorio renombrarlos aquí.

## Pasos

### 1. Renombrar `OffersFragment`

Aplicá los renombres dentro de la clase, incluyendo lambdas y callbacks. No cambies literales de estado (`buyer`, `seller`, `pending`, etc.) ni mensajes.

**Verificar**: compilar y ejecutar `rg -n '\b(loadOffers|renderOffers|createCard|showCounterDialog|sellerAction|counterAction|cancelOffer|actionResult|setLoading|addAction|optionalText|statusLabel|expiryText|selectedRole|loading|firstResume)\b' app/src/main/java/com/da_grupo9/ronda/ui/fragments/OffersFragment.java`; sólo `formatDate` y nombres impuestos por callbacks pueden quedar fuera de la lista objetivo.

### 2. Renombrar los fragments restantes

Aplicá los renombres mínimos indicados. En `OperationDetailFragment`, actualizá todas las lecturas y escrituras de los campos, sin cambiar getters de `Operation`.

**Verificar**: `.\gradlew.bat compileDebugJavaWithJavac --console=plain` termina con código 0.

### 3. Ejecutar verificaciones

Ejecutá `testDebugUnitTest`, `lintDebug` y `git diff --check`. Revisá el diff para confirmar que cada hunk sea un renombre, sin cambios de condiciones ni textos.

## Plan de pruebas

No agregues tests para renombres privados. La compilación cubre todas las referencias Java. Los tests y lint existentes deben seguir pasando.

## Criterios de finalización

- [ ] Los símbolos mínimos fueron renombrados.
- [ ] No cambiaron métodos de framework, DTO, valores de API ni IDs XML.
- [ ] El diff no altera lógica o textos.
- [ ] Compilación, tests y lint terminan con código 0.
- [ ] El plan figura `DONE`.

## Condiciones de parada

- Un símbolo objetivo forma parte de una interfaz pública consumida fuera del módulo.
- El plan 003 no está completado y todavía hay claves de navegación inglesas relacionadas.
- La compilación exige modificar archivos fuera del alcance.

## Notas de mantenimiento

La convención no exige traducir APIs o términos técnicos. El objetivo es que acciones y conceptos internos del dominio usen un vocabulario único y buscable.
