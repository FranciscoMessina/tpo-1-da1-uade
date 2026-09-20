# Plan 003: Unificar los argumentos internos de navegación en español

> **Instrucciones para el ejecutor**: seguí los pasos en orden, ejecutá cada verificación y actualizá `plans/README.md` al terminar. No improvises ante una condición de parada.
>
> **Control de deriva inicial**: `git diff --stat df592cd..HEAD -- app/src/main/res/navigation/nav_graph.xml app/src/main/java/com/da_grupo9/ronda/ui/fragments`
> Los planes 001 y 002 pueden haber cambiado imports y callbacks. Eso es esperado; detenete sólo si cambió el nombre o significado de los argumentos descritos aquí.

## Estado

- **Prioridad**: P1
- **Esfuerzo**: M
- **Riesgo**: MED
- **Depende de**: `plans/002-encapsular-retrofit-en-repositorios.md`
- **Categoría**: tech-debt
- **Planificado en**: commit `df592cd`, 2026-09-20

## Por qué importa

Los argumentos de Navigation usan strings escritos manualmente. La misma publicación se identifica como `publicacionId` o `publicationId`, y el título y vendedor cambian de idioma según el destino. Una convención única reduce errores silenciosos al construir `Bundle` y vuelve predecible el contrato entre fragments.

## Estado actual

- `nav_graph.xml` usa `publicacionId` para detalle, edición y preguntas, pero `publicationId` para crear ofertas.
- `DetailFragment:374-399` produce las dos variantes.
- `CreateOfferFragment:53-55` consume `publicationId`, `publicationTitle` y `sellerName`.
- También existen argumentos internos en inglés: `returnToProfile`, `purpose`, `operation` y `selectedRole`.
- `email` queda sin traducir porque es vocabulario técnico compartido y no presenta una variante conflictiva.
- Los valores de dominio enviados al servidor (`buyer`, `seller`, `login`, `registration`, `set_password`) no son nombres Java y deben conservarse exactamente.

## Convención objetivo

| Actual | Objetivo |
|--------|----------|
| `publicationId` | `publicacionId` |
| `publicationTitle` | `publicacionTitulo` |
| `publicationPrice` | `publicacionPrecio` |
| `sellerName` | `vendedorNombre` |
| `selectedRole` | `rolSeleccionado` |
| `returnToProfile` | `volverAlPerfil` |
| `purpose` | `proposito` |
| `operation` | `operacion` |

Los argumentos ya escritos en español (`usuarioId`, `vendedorReputacion`) permanecen iguales.

## Comandos

| Propósito | Comando | Resultado esperado |
|-----------|---------|--------------------|
| Compilar | `.\gradlew.bat compileDebugJavaWithJavac --console=plain` | código 0 |
| Tests | `.\gradlew.bat testDebugUnitTest --console=plain` | código 0 |
| Lint | `.\gradlew.bat lintDebug --console=plain` | código 0 |

## Alcance

**Permitidos:** `app/src/main/res/navigation/nav_graph.xml` y únicamente los fragments que leen o escriben los argumentos de la tabla: `CreateOfferFragment`, `DetailFragment`, `OffersFragment`, `ForgotPasswordFragment`, `ProfileFragment`, `ResetPasswordFragment`, `OtpFragment`, `LoginFragment`, `RegisterFragment`, `OperationDetailFragment` y `OperationsHistoryFragment`; además de `plans/README.md`.

**Fuera de alcance:** nombres JSON/Retrofit, valores de roles o propósitos, IDs de recursos, argumentos que ya están en español, Safe Args o cambios de navegación.

## Pasos

### 1. Actualizar el grafo

Renombrá cada declaración de la tabla en `nav_graph.xml`. No cambies tipos, defaults, nullability, destinos ni acciones.

**Verificar**: `rg -n 'android:name="(publicationId|publicationTitle|publicationPrice|sellerName|selectedRole|returnToProfile|purpose|operation)"' app/src/main/res/navigation/nav_graph.xml` no devuelve resultados.

### 2. Actualizar productores y consumidores

Reemplazá cada `put*`, `get*` y `requireArguments().get*` con la clave objetivo. Hacé el reemplazo por pares completos: productor y consumidor deben cambiar en el mismo paso.

En particular, preservá:

- navegación detalle → oferta y detalle → pregunta;
- perfil → restablecer contraseña;
- login/registro → OTP;
- historial → detalle de operación;
- oferta creada → lista filtrada por rol comprador.

**Verificar**: `rg -n '"(publicationId|publicationTitle|publicationPrice|sellerName|selectedRole|returnToProfile|purpose|operation)"' app/src/main/java app/src/main/res/navigation` no devuelve resultados. Coincidencias en nombres de campos DTO sin comillas no forman parte de este plan.

### 3. Validar el contrato completo

Buscá cada clave objetivo en `nav_graph.xml` y Java. Toda clave debe tener al menos una declaración y un consumidor; las que transportan datos desde otro fragment deben tener también un productor.

Ejecutá compilación, tests, lint y `git diff --check`.

## Plan de pruebas

No agregues tests que sólo comparen strings constantes. La verificación principal es compilación de Navigation y búsquedas estructurales. Como comprobación manual posterior: abrir detalle, pregunta, oferta, perfil público, historial, OTP y restablecimiento de contraseña, confirmando que conservan sus datos.

## Criterios de finalización

- [ ] No quedan las ocho claves inglesas de la tabla en Java o navegación.
- [ ] Los valores `buyer`, `seller` y los propósitos de autenticación no cambiaron.
- [ ] Compilación, tests y lint terminan con código 0.
- [ ] Sólo se modificaron archivos permitidos.
- [ ] El plan figura `DONE`.

## Condiciones de parada

- Una clave también se usa como nombre exigido por una API externa.
- Un productor no puede identificarse para alguno de los argumentos requeridos.
- Navigation genera un error que obliga a cambiar destinos o tipos.
- Una verificación falla dos veces tras un intento razonable.

## Notas de mantenimiento

Las claves de `Bundle` son contratos internos: nuevos argumentos de dominio deben escribirse en español. Los valores transmitidos pueden seguir en inglés cuando forman parte del contrato del backend.
