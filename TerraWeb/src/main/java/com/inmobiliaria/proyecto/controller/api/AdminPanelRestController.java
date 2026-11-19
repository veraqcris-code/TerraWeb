package com.inmobiliaria.proyecto.controller.api;

import java.util.List;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.inmobiliaria.proyecto.dto.admin.AgenteAdminFormDto;
import com.inmobiliaria.proyecto.dto.admin.AgenteAdminResumenDto;
import com.inmobiliaria.proyecto.dto.admin.CambiarPasswordAdminDto;
import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminFormDto;
import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminOpcionDto;
import com.inmobiliaria.proyecto.dto.propiedad.PropiedadDetalleDto;
import com.inmobiliaria.proyecto.dto.admin.SolicitudEstadoAdminDto;
import com.inmobiliaria.proyecto.dto.admin.SolicitudVisitaAdminDto;
import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaDto;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.service.AgenteService;
import com.inmobiliaria.proyecto.service.AdminDashboardService;
import com.inmobiliaria.proyecto.service.ConfiguracionAdminService;
import com.inmobiliaria.proyecto.service.PropiedadService;
import com.inmobiliaria.proyecto.service.SolicitudVisitaService;
import com.inmobiliaria.proyecto.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminPanelRestController {

    private final AdminDashboardService dashboardService;
    private final SolicitudVisitaService solicitudVisitaService;
    private final PropiedadService propiedadService;
    private final AgenteService agenteService;
    private final ConfiguracionAdminService configuracionService;
    private final UsuarioService usuarioService;
    private final SessionRegistry sessionRegistry;

    public AdminPanelRestController(
            AdminDashboardService dashboardService,
            SolicitudVisitaService solicitudVisitaService,
            PropiedadService propiedadService,
            AgenteService agenteService,
            ConfiguracionAdminService configuracionService,
            UsuarioService usuarioService,
            SessionRegistry sessionRegistry) {
        this.dashboardService = dashboardService;
        this.solicitudVisitaService = solicitudVisitaService;
        this.propiedadService = propiedadService;
        this.agenteService = agenteService;
        this.configuracionService = configuracionService;
        this.usuarioService = usuarioService;
        this.sessionRegistry = sessionRegistry;
    }

    @GetMapping("/solicitudes")
    public List<SolicitudVisitaAdminDto> solicitudes() {
        return solicitudVisitaService.listarTodas();
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudVisitaAdminDto> crearSolicitud(@Valid @RequestBody SolicitudVisitaDto datos) {
        Long id = solicitudVisitaService.registrar(datos.getPropiedadId(), datos).id();
        SolicitudVisitaAdminDto creada = solicitudVisitaService.obtener(id);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(creada.id())
                        .toUri())
                .body(creada);
    }

    @GetMapping("/solicitudes/{id}")
    public SolicitudVisitaAdminDto solicitud(@PathVariable Long id) {
        return solicitudVisitaService.obtener(id);
    }

    @PutMapping("/solicitudes/{id}")
    public SolicitudVisitaAdminDto actualizarSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudVisitaDto datos) {
        return solicitudVisitaService.actualizar(id, datos);
    }

    @DeleteMapping("/solicitudes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarSolicitud(@PathVariable Long id) {
        solicitudVisitaService.eliminar(id);
    }

    @PutMapping("/solicitudes/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizarEstadoSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudEstadoAdminDto datos) {
        solicitudVisitaService.actualizarEstado(id, datos.estado());
    }

    @GetMapping("/agentes")
    public List<AgenteAdminResumenDto> agentes() {
        List<Propiedad> propiedades = propiedadService.listarTodas();
        return agenteService.listarTodos().stream()
                .map(agente -> resumirAgente(agente, propiedades))
                .toList();
    }

    @GetMapping("/agentes/{id}")
    public AgenteAdminResumenDto agente(@PathVariable Long id) {
        Agente agente = agenteService.obtenerEntidad(id);
        return resumirAgente(agente, propiedadService.listarTodas());
    }

    @PostMapping("/agentes")
    public ResponseEntity<AgenteAdminResumenDto> crearAgente(@Valid @RequestBody AgenteAdminFormDto datos) {
        Agente agente = agenteService.guardar(null, datos);
        AgenteAdminResumenDto respuesta = resumirAgente(agente, propiedadService.listarTodas());
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(agente.getId())
                        .toUri())
                .body(respuesta);
    }

    @PutMapping("/agentes/{id}")
    public AgenteAdminResumenDto actualizarAgente(
            @PathVariable Long id,
            @Valid @RequestBody AgenteAdminFormDto datos) {
        Agente agente = agenteService.guardar(id, datos);
        return resumirAgente(agente, propiedadService.listarTodas());
    }

    @DeleteMapping("/agentes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAgente(@PathVariable Long id) {
        agenteService.eliminar(id);
    }

    private AgenteAdminResumenDto resumirAgente(Agente agente, List<Propiedad> propiedades) {
        long asignadas = propiedades.stream()
                .filter(propiedad -> propiedad.getAgente() != null
                        && propiedad.getAgente().getId().equals(agente.getId()))
                .count();
        return new AgenteAdminResumenDto(
                agente.getId(),
                agente.getNombre(),
                agente.getCorreo(),
                agente.getCargo(),
                agente.getEspecialidad(),
                agente.getEstado(),
                agente.getFotoUrl(),
                agente.getTelefono(),
                agente.isActivo(),
                asignadas);
    }

    @GetMapping("/propiedades")
    public List<PropiedadDetalleDto> propiedades() {
        return propiedadService.listarDetallesAdmin();
    }

    @GetMapping("/propiedades/opciones")
    public List<PropiedadAdminOpcionDto> propiedadesOpcion() {
        return propiedadService.listarTodas().stream()
                .filter(propiedad -> propiedad.isPublicada())
                .map(propiedad -> new PropiedadAdminOpcionDto(propiedad.getId(), propiedad.getTitulo()))
                .toList();
    }

    @GetMapping("/propiedades/{id}")
    public PropiedadDetalleDto propiedad(@PathVariable Long id) {
        return propiedadService.obtenerDetalleAdmin(id);
    }

    @PostMapping("/propiedades")
    public ResponseEntity<PropiedadDetalleDto> crearPropiedad(
            @Valid @RequestBody PropiedadAdminFormDto datos) {
        var propiedad = propiedadService.guardarDetalle(null, datos, List.of());
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(propiedad.id())
                        .toUri())
                .body(propiedad);
    }

    @PutMapping("/propiedades/{id}")
    public PropiedadDetalleDto actualizarPropiedad(
            @PathVariable Long id,
            @Valid @RequestBody PropiedadAdminFormDto datos) {
        return propiedadService.guardarDetalle(id, datos, List.of());
    }

    @DeleteMapping("/propiedades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPropiedad(@PathVariable Long id) {
        propiedadService.eliminar(id);
    }

    @GetMapping("/configuracion")
    public ConfiguracionAdminDto configuracion() {
        return configuracionService.obtener();
    }

    @PutMapping("/configuracion")
    public ConfiguracionAdminDto guardarConfiguracion(@Valid @RequestBody ConfiguracionAdminDto datos) {
        return configuracionService.guardar(datos);
    }

    /**
     * Changes the authenticated administrator's password.
     *
     * @param datos the current and new password values
     * @param authentication the authenticated administrator
     * @return no content on success or an error status when verification fails
     */
    @PostMapping("/configuracion/password")
    public void cambiarPassword(
            @Valid @RequestBody CambiarPasswordAdminDto datos,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (!datos.passwordNueva().equals(datos.confirmarPassword())) {
            try {
                writeJsonError(response, 400, "La confirmación no coincide con la nueva contraseña",
                        Map.of("confirmarPassword", "Las contraseñas no coinciden"));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        boolean actualizado = usuarioService.cambiarPassword(
                authentication.getName(), datos.passwordActual(), datos.passwordNueva());
        if (actualizado && datos.cerrarOtrasSesiones()) {
            HttpSession sesionActual = request.getSession(false);
            if (sesionActual != null) {
                sessionRegistry.getAllSessions(authentication.getPrincipal(), false).stream()
                        .filter(sesion -> !sesion.getSessionId().equals(sesionActual.getId()))
                        .forEach(SessionInformation::expireNow);
            }
        }
        if (actualizado) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            try {
                writeJsonError(response, 401, "La contraseña actual no es correcta",
                        Map.of("passwordActual", "La contraseña actual no es correcta"));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void writeJsonError(HttpServletResponse response, int status, String mensaje, Map<String, String> errores) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String fechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fecha\":\"").append(fechaIso).append("\",");
        sb.append("\"estado\":").append(status).append(",");
        sb.append("\"mensaje\":\"").append(escapeJson(mensaje)).append("\",");
        sb.append("\"errores\":{");
        boolean first = true;
        for (Map.Entry<String, String> entry : errores.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}}");
        response.getWriter().write(sb.toString());
    }
}
