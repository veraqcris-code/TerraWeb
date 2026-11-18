package com.inmobiliaria.proyecto.controller.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.inmobiliaria.proyecto.dto.admin.AgenteAdminResumenDto;
import com.inmobiliaria.proyecto.dto.admin.CambiarPasswordAdminDto;
import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminOpcionDto;
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
    @ResponseStatus(HttpStatus.CREATED)
    public void crearSolicitud(@Valid @RequestBody SolicitudVisitaDto datos) {
        solicitudVisitaService.registrar(datos.getPropiedadId(), datos);
    }

    @PutMapping("/solicitudes/{id}/estado")
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
    public List<PropiedadAdminOpcionDto> propiedades() {
        return propiedadService.listarTodas().stream()
                .filter(propiedad -> propiedad.isPublicada())
                .map(propiedad -> new PropiedadAdminOpcionDto(propiedad.getId(), propiedad.getTitulo()))
                .toList();
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
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody CambiarPasswordAdminDto datos,
            Authentication authentication,
            HttpServletRequest request) {
        if (!datos.passwordNueva().equals(datos.confirmarPassword())) {
            return ResponseEntity.badRequest().build();
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
        return actualizado
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
