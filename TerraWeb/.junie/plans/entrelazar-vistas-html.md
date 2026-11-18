---
sessionId: session-261117-000810-1eys
---

# Requirements

### Overview
Las vistas `inicio.html`, `busqueda.html`, `agente.html` y `nosotros.html` no tienen navegación funcional entre sí. Todos los enlaces de los encabezados y pies de página apuntan a `#` sin destino real, y no existen controladores Spring para las rutas de búsqueda, agentes y nosotros.

### Goals
- Permitir navegar entre las cuatro vistas desde el menú de navegación y el pie de página.
- Crear los controladores Spring faltantes para `/busqueda`, `/agentes` y `/nosotros`.
- Unificar los enlaces de navegación usando Thymeleaf `th:href` para que las URLs se resuelvan correctamente.

### Scope
**In Scope:**
- Crear 3 controladores: `BusquedaController`, `AgentesController`, `NosotrosController`.
- Actualizar los `<header>` y `<footer>` de las 4 vistas para que los enlaces apunten a las rutas correctas.
- Reemplazar `href="#"` por `th:href="@{/ruta}"` en los menús y pies de página.

**Out of Scope:**
- Modificar el diseño visual de las vistas.
- Crear nuevas vistas o funcionalidades.
- Modificar las vistas de admin, login, registro, o perfil.

# Technical Design

### Current Implementation
- `HomeController.java` expone `/` y `/inicio` → renderiza `inicio.html`.
- No existen controladores para `/busqueda`, `/agentes`, ni `/nosotros`.
- Las 4 vistas tienen menús de navegación con `data-path` y `href="#"` (sin destino).
- `inicio.html` usa Thymeleaf (`xmlns:th`), mientras que `busqueda.html`, `agente.html` y `nosotros.html` no declaran el namespace Thymeleaf pero son renderizados por Spring (templates).

### Key Decisions
- Usar `@Controller` con `@GetMapping` simple siguiendo el patrón de `HomeController`.
- Usar `th:href="@{/ruta}"` en lugar de URLs relativas para que Spring resuelva las rutas correctamente.
- Agregar el namespace Thymeleaf a las vistas que no lo tienen (`busqueda.html`, `agente.html`, `nosotros.html`).

### Proposed Changes

#### 1. Nuevos controladores
- `BusquedaController` → `@GetMapping("/busqueda")` retorna `"busqueda"`.
- `AgentesController` → `@GetMapping("/agentes")` retorna `"agente"`.
- `NosotrosController` → `@GetMapping("/nosotros")` retorna `"nosotros"`.

#### 2. Enlaces de navegación (header) en cada vista
Las rutas mapeadas son:
- `inicio` → `th:href="@{/inicio}"`
- `buscar` → `th:href="@{/busqueda}"`
- `agentes` → `th:href="@{/agentes}"`
- `quienes-somos` → `th:href="@{/nosotros}"`

#### 3. Enlaces del pie de página (footer)
Mismo mapeo que el header para los enlaces que usan `data-path`.

### Architecture Diagram

``` mermaid
graph LR
  A[inicio.html] -->|/inicio| B[HomeController]
  C[busqueda.html] -->|/busqueda| D[BusquedaController]
  E[agente.html] -->|/agentes| F[AgentesController]
  G[nosotros.html] -->|/nosotros| H[NosotrosController]
  B --> A
  D --> C
  F --> E
  H --> G
```

### File Structure
**Nuevos archivos:**
- `src/main/java/com/inmobiliaria/proyecto/controller/BusquedaController.java`
- `src/main/java/com/inmobiliaria/proyecto/controller/AgentesController.java`
- `src/main/java/com/inmobiliaria/proyecto/controller/NosotrosController.java`

**Modificados:**
- `src/main/resources/templates/inicio.html` — header nav + footer links
- `src/main/resources/templates/busqueda.html` — agregar namespace th, header nav + footer links
- `src/main/resources/templates/agente.html` — agregar namespace th, header nav + footer links
- `src/main/resources/templates/nosotros.html` — agregar namespace th, header nav + footer links

# Delivery Steps

###   Step 1: Crear controladores para busqueda, agentes y nosotros
Se crean tres controladores Spring que exponen las rutas `/busqueda`, `/agentes` y `/nosotros`, renderizando sus respectivas plantillas HTML.
- Crear `BusquedaController.java` con `@GetMapping("/busqueda")` que retorna `"busqueda"`.
- Crear `AgentesController.java` con `@GetMapping("/agentes")` que retorna `"agente"`.
- Crear `NosotrosController.java` con `@GetMapping("/nosotros")` que retorna `"nosotros"`.
- Validar que la aplicación compila correctamente con `mvnw test-compile`.

###   Step 2: Actualizar navegación en inicio.html
Se reemplazan los enlaces `href="#"` del header y footer por `th:href` apuntando a las rutas correctas.
- Header: Inicio → `@{/inicio}`, Buscar → `@{/busqueda}`, Agentes → `@{/agentes}`, Quiénes somos → `@{/nosotros}`.
- Footer: todos los enlaces con `data-path="inicio"` → `@{/inicio}`, `data-path="buscar"` → `@{/busqueda}`, `data-path="agentes"` → `@{/agentes}`, `data-path="quienes-somos"` → `@{/nosotros}`.
- Se mantiene `target="_self"` en el botón de perfil.

###   Step 3: Actualizar navegación en busqueda.html
Se agrega el namespace Thymeleaf y se conectan los enlaces de navegación.
- Agregar `xmlns:th="http://www.thymeleaf.org"` al tag `<html>`.
- Header: los enlaces con `data-path` se reemplazan con `th:href` a las rutas correspondientes.
- Footer: mismo mapeo de `data-path` a `th:href`.

###   Step 4: Actualizar navegación en agente.html y nosotros.html
Se aplica el mismo patrón de enlaces Thymeleaf en ambas vistas restantes.
- Agregar namespace Thymeleaf al tag `<html>`.
- Header y footer: reemplazar `href="#"` por `th:href="@{/ruta}"` según el `data-path` de cada enlace.
- Validar que la compilación exitosa y que las 4 vistas son accesibles desde las rutas definidas.