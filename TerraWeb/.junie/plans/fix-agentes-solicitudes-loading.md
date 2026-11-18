---
sessionId: session-261116-223822-5j5f
---

# Diagnóstico del Problema

### Problema Reportado
Los registros de **agentes** y **solicitudes** que aparecen correctamente en `resumen.html` no se muestran en las tablas de `solicitud.html` y `agente.html`. En cambio, las **propiedades** sí se cargan correctamente en `propiedades.html`.

### Raíz Causa Identificada

El problema radica en la diferencia de cómo se cargan los datos entre las vistas:

| Vista | Mecanismo | Funciona |
|---|---|---|
| `resumen.html` | Thymeleaf + `AdminDashboardService.obtenerResumen()` con `@Transactional` en toda la cadena | ✅ Sí |
| `propiedades.html` | Thymeleaf + `AdminPropiedadController.listar()` con `@EntityGraph` en repository | ✅ Sí |
| `solicitud.html` | JavaScript `fetch("/api/v1/admin/solicitudes")` → `SolicitudVisitaService.listarTodas()` | ❌ No |
| `agente.html` | JavaScript `fetch("/api/v1/admin/agentes")` → `dashboardService.obtenerResumen().agentes()` | ❌ No |

### Causas técnicas específicas

**1. `LazyInitializationException` en `SolicitudVisitaService.aDto()` (línea 96-114)**

El método `aDto()` accede a `solicitud.getPropiedad()` que es una relación `@ManyToOne(fetch = FetchType.LAZY)`. El repository `SolicitudVisitaRepository.findAllByOrderByFechaSolicitudDesc()` **no tiene `@EntityGraph`** para cargar `propiedad` y `propiedad.agente` de forma eager. La configuración `spring.jpa.open-in-view=false` (`application.properties` línea 13) significa que la sesión Hibernate solo está activa dentro del método transaccional. Si hay cualquier problema con la propagación de la transacción, el acceso LAZY falla.

**2. Sin `@RestControllerAdvice` para `AdminPanelRestController`**

El `ApiExceptionHandler` actual solo aplica a `PropiedadRestController`:
```java
@RestControllerAdvice(basePackageClasses = PropiedadRestController.class)
```
Las excepciones en `AdminPanelRestController` devuelven un error 500 sin formato JSON estructurado, y el frontend lo captura como un error genérico.

**3. `AdminPanelRestController.agentes()` llama a `dashboardService.obtenerResumen()` completo**

Para obtener solo la lista de agentes, el endpoint ejecuta `obtenerResumen()` que carga **todas las propiedades con imágenes y agentes** (vía `@EntityGraph`), todas las solicitudes activas y archivadas. Esto es ineficiente y propenso a errores en cascada.

### Archivos Clave Involucrados
- `AdminPanelRestController.java` — endpoints REST del panel admin
- `SolicitudVisitaService.java` — servicio de solicitudes, método `aDto()` con acceso LAZY
- `SolicitudVisitaRepository.java` — sin `@EntityGraph` en las consultas
- `AdminDashboardService.java` — servicio del dashboard
- `AgenteService.java` — servicio de agentes
- `ApiExceptionHandler.java` — solo cubre `PropiedadRestController`
- `application.properties` — `spring.jpa.open-in-view=false`


# Cambios Propuestos

### Cambio 1: Agregar `@EntityGraph` a `SolicitudVisitaRepository`

**Archivo:** `SolicitudVisitaRepository.java`

Agregar `@EntityGraph(attributePaths = {"propiedad", "propiedad.agente"})` a los métodos `findAllByOrderByFechaSolicitudDesc()` y `findByArchivadaOrderByFechaSolicitudDesc()` para cargar la relación `propiedad` y su `agente` de forma eager, eliminando el riesgo de `LazyInitializationException`.

Esto es consistente con el patrón ya usado en `PropiedadRepository.findAllByOrderByFechaPublicacionDesc()` que usa `@EntityGraph(attributePaths = {"imagenes", "agente"})`.

### Cambio 2: Crear endpoint dedicado para agentes en `AdminPanelRestController`

**Archivo:** `AdminPanelRestController.java`

Reemplazar el endpoint `/api/v1/admin/agentes` para que invoque directamente a `agenteService.listarTodos()` y mapee a `AgenteAdminResumenDto`, en lugar de llamar a `dashboardService.obtenerResumen()` completo. Esto:
- Evita cargar todas las propiedades e imágenes innecesariamente
- Reduce la probabilidad de errores en cascada
- Mejora el rendimiento

### Cambio 3: Extender `ApiExceptionHandler` para cubrir `AdminPanelRestController`

**Archivo:** `ApiExceptionHandler.java`

Cambiar el `@RestControllerAdvice` para que también cubra `AdminPanelRestController`, de modo que las excepciones como `LazyInitializationException` o `RecursoNoEncontradoException` devuelvan una respuesta JSON estructurada en lugar de un error 500 genérico.

### Resumen de archivos a modificar
| Archivo | Acción |
|---|---|
| `SolicitudVisitaRepository.java` | Agregar `@EntityGraph` a 2 métodos |
| `AdminPanelRestController.java` | Optimizar endpoint de agentes, inyectar `AgenteService` |
| `ApiExceptionHandler.java` | Extender cobertura a controladores admin |


# Delivery Steps

### ✅ Step 1: Agregar @EntityGraph a SolicitudVisitaRepository
**Estado: Completado**

Agregar `@EntityGraph` para cargar eager la relación `propiedad` y `propiedad.agente` en las consultas de solicitudes.
- Modificar `SolicitudVisitaRepository.java`: agregar `@EntityGraph(attributePaths = {"propiedad", "propiedad.agente"})` a `findAllByOrderByFechaSolicitudDesc()` y `findByArchivadaOrderByFechaSolicitudDesc()`.
- Esto elimina el riesgo de `LazyInitializationException` al acceder a `solicitud.getPropiedad()` en `SolicitudVisitaService.aDto()`.
- Sigue el mismo patrón que ya usa `PropiedadRepository.findAllByOrderByFechaPublicacionDesc()`.

### ✅ Step 2: Optimizar endpoint de agentes y extender ApiExceptionHandler
**Estado: Completado**

Mejorar el endpoint de agentes y la gestión de excepciones del panel admin.
- En `AdminPanelRestController.java`: inyectar `AgenteService`, reemplazar `dashboardService.obtenerResumen().agentes()` por una llamada directa a `agenteService.listarTodos()` mapeada a `AgenteAdminResumenDto` mediante un método `resumirAgente()` privado.
- En `ApiExceptionHandler.java`: cambiar `basePackageClasses` para incluir `AdminPanelRestController.class` además de `PropiedadRestController.class`.
- Verificar que ambos endpoints (`/api/v1/admin/solicitudes` y `/api/v1/admin/agentes`) devuelvan datos correctamente.

### ✅ Step 3: Corregir Thymeleaf inline en scripts de solicitud.html y agente.html
**Estado: Completado**

La causa raíz real fue `TemplateProcessingException`: Thymeleaf interpretaba los arreglos anidados `[[...]]` de JavaScript como expresiones inline.
- En `solicitud.html`: agregar `th:inline="none"` en la etiqueta `<script>` (línea 87) para evitar que `[[["NUEVA", "Nueva"], ...]]` sea parseado como expresión Thymeleaf.
- En `agente.html`: agregar `th:inline="none"` en la etiqueta `<script>` (línea 71) para prevenir problemas similares.
- Ambas vistas ahora se renderizan correctamente y el JavaScript puede cargar datos desde los endpoints REST sin interferencia de Thymeleaf.

### Verificaciones adicionales realizadas

**Spring Security:** Los endpoints `/api/v1/admin/**` requieren `hasRole("ADMIN")` (`SecurityConfig.java` línea 37). Las peticiones `fetch()` desde `solicitud.html` y `agente.html` funcionan correctamente porque el usuario administrador está autenticado y tiene el rol `ADMIN`. No hay bloqueo de Spring Security.

**Nombres de campos JSON:**
- `SolicitudVisitaAdminDto` campos: `id`, `nombre`, `telefono`, `correo`, `tituloPropiedad`, `fechaEstimada`, `fechaSolicitud`, `archivada`, `estado`, `agenteId`, `agenteNombre` → coinciden exactamente con lo que espera el JavaScript de `solicitud.html`.
- `AgenteAdminResumenDto` campos: `id`, `nombre`, `correo`, `cargo`, `especialidad`, `estado`, `fotoUrl`, `telefono`, `activo`, `propiedadesAsignadas` → coinciden exactamente con lo que espera el JavaScript de `agente.html`.

**Carga de `propiedad.getAgente()` en el endpoint de agentes:** `PropiedadService.listarTodas()` usa `@EntityGraph(attributePaths = {"imagenes", "agente"})` en el repository, por lo que la relación `agente` ya está materializada cuando se accede desde `resumirAgente()`. No hay riesgo de `LazyInitializationException`.