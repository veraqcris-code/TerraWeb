package com.inmobiliaria.proyecto.controller.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.math.BigDecimal;

import org.springframework.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminFormDto;
import com.inmobiliaria.proyecto.exception.ImagenInvalidaException;
import com.inmobiliaria.proyecto.models.ImagenPropiedad;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.service.AgenteService;
import com.inmobiliaria.proyecto.service.ImagenStorageService;
import com.inmobiliaria.proyecto.service.PropiedadService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/propiedades")
public class AdminPropiedadController {

    private final PropiedadService propiedadService;
    private final AgenteService agenteService;
    private final ImagenStorageService imagenStorageService;

    public AdminPropiedadController(
            PropiedadService propiedadService,
            AgenteService agenteService,
            ImagenStorageService imagenStorageService) {
        this.propiedadService = propiedadService;
        this.agenteService = agenteService;
        this.imagenStorageService = imagenStorageService;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "TODAS") String estado,
            @RequestParam(defaultValue = "") String operacion,
            @RequestParam(defaultValue = "") String distrito,
            @RequestParam(defaultValue = "") String tipo,
            @RequestParam(defaultValue = "") String precio,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        List<Propiedad> todas = propiedadService.listarTodas();
        List<Propiedad> filtradas = todas.stream()
                .filter(propiedad -> coincide(buscar, propiedad.getTitulo(), propiedad.getDireccion(),
                        propiedad.getDistrito(), propiedad.getCiudad()))
                .filter(propiedad -> coincide(operacion, propiedad.getModalidad()))
                .filter(propiedad -> coincide(distrito, propiedad.getDistrito()))
                .filter(propiedad -> coincide(tipo, propiedad.getTipo()))
                .filter(propiedad -> coincideEstado(estado, propiedad))
                .filter(propiedad -> coincidePrecio(precio, propiedad.getPrecio()))
                .toList();
        int tamanioPagina = 10;
        int paginaActual = Math.max(0, pagina);
        int totalPaginas = Math.max(1, (int) Math.ceil(filtradas.size() / (double) tamanioPagina));
        paginaActual = Math.min(paginaActual, totalPaginas - 1);
        int desde = Math.min(paginaActual * tamanioPagina, filtradas.size());

        model.addAttribute("propiedades", filtradas.subList(desde,
                Math.min(desde + tamanioPagina, filtradas.size())));
        model.addAttribute("totalPropiedades", filtradas.size());
        model.addAttribute("totalRegistradas", todas.size());
        model.addAttribute("pagina", paginaActual);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("buscar", buscar);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("operacionFiltro", operacion);
        model.addAttribute("distritoFiltro", distrito);
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("precioFiltro", precio);
        model.addAttribute("operaciones", todas.stream().map(Propiedad::getModalidad)
                .filter(StringUtils::hasText).distinct().sorted().toList());
        model.addAttribute("distritos", todas.stream().map(Propiedad::getDistrito)
                .filter(StringUtils::hasText).distinct().sorted().toList());
        model.addAttribute("tipos", todas.stream().map(Propiedad::getTipo)
                .filter(StringUtils::hasText).distinct().sorted().toList());
        model.addAttribute("disponibles", todas.stream().filter(p -> coincideEstado("DISPONIBLES", p)).count());
        model.addAttribute("reservadas", todas.stream().filter(p -> coincideEstado("RESERVADAS", p)).count());
        model.addAttribute("retiradas", todas.stream().filter(p -> !p.isPublicada()).count());
        return "admin/propiedades";
    }

    private boolean coincide(String filtro, String... valores) {
        if (!StringUtils.hasText(filtro)) {
            return true;
        }
        String buscado = filtro.trim().toLowerCase(Locale.ROOT);
        return java.util.Arrays.stream(valores)
                .filter(StringUtils::hasText)
                .anyMatch(valor -> valor.toLowerCase(Locale.ROOT).contains(buscado));
    }

    private boolean coincideEstado(String filtro, Propiedad propiedad) {
        if (!StringUtils.hasText(filtro) || filtro.equalsIgnoreCase("TODAS")) {
            return propiedad.isPublicada();
        }
        String estado = propiedad.getEstado();
        if (estado == null) {
            return false;
        }
        String valor = estado.toUpperCase(Locale.ROOT);
        if (filtro.equalsIgnoreCase("DISPONIBLES")) {
            return propiedad.isPublicada() && valor.contains("DISPONIBLE");
        }
        if (filtro.equalsIgnoreCase("RESERVADAS")) {
            return propiedad.isPublicada() && valor.contains("RESERVAD");
        }
        if (filtro.equalsIgnoreCase("RETIRADAS")) {
            return !propiedad.isPublicada();
        }
        return false;
    }

    private boolean coincidePrecio(String filtro, BigDecimal precio) {
        if (!StringUtils.hasText(filtro) || precio == null) {
            return true;
        }
        return switch (filtro) {
            case "0-200000" -> precio.compareTo(new BigDecimal("200000")) < 0;
            case "200000-500000" -> precio.compareTo(new BigDecimal("200000")) >= 0
                    && precio.compareTo(new BigDecimal("500000")) <= 0;
            case "500000+" -> precio.compareTo(new BigDecimal("500000")) > 0;
            default -> true;
        };
    }

    @GetMapping("/nueva")
    public String nueva(
            @RequestParam(name = "returnTo", defaultValue = "/admin/propiedades") String returnTo,
            Model model) {
        model.addAttribute("propiedadForm", new PropiedadAdminFormDto());
        model.addAttribute("formAction", "/admin/propiedades");
        model.addAttribute("returnTo", normalizarRetorno(returnTo));
        cargarAgentes(model);
        cargarImagenes(model, null, null);
        return "admin/propiedad-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            @RequestParam(name = "returnTo", defaultValue = "/admin/propiedades") String returnTo,
            Model model) {
        model.addAttribute("propiedadForm", propiedadService.cargarFormulario(id));
        model.addAttribute("propiedadId", id);
        model.addAttribute("formAction", "/admin/propiedades/" + id);
        model.addAttribute("returnTo", normalizarRetorno(returnTo));
        cargarAgentes(model);
        cargarImagenes(model, id, null);
        return "admin/propiedad-form";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("propiedadForm") PropiedadAdminFormDto formulario,
            BindingResult resultado,
            @RequestParam(name = "imagenes", required = false) List<MultipartFile> imagenes,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "returnTo", defaultValue = "/admin/propiedades") String returnTo) {
        if (resultado.hasErrors()) {
            model.addAttribute("formAction", "/admin/propiedades");
            model.addAttribute("returnTo", normalizarRetorno(returnTo));
            cargarAgentes(model);
            cargarImagenes(model, null, null);
            return "admin/propiedad-form";
        }
        List<String> rutasImagenes;
        try {
            rutasImagenes = imagenStorageService.guardar(imagenes, PropiedadService.MAX_IMAGENES);
        } catch (ImagenInvalidaException excepcion) {
            resultado.reject("imagenes.invalidas", excepcion.getMessage());
            model.addAttribute("formAction", "/admin/propiedades");
            model.addAttribute("returnTo", normalizarRetorno(returnTo));
            cargarAgentes(model);
            cargarImagenes(model, null, null);
            return "admin/propiedad-form";
        }
        try {
            propiedadService.guardar(null, formulario, rutasImagenes, List.of());
        } catch (RuntimeException excepcion) {
            imagenStorageService.eliminar(rutasImagenes);
            throw excepcion;
        }
        redirectAttributes.addFlashAttribute("mensaje", "Propiedad creada correctamente.");
        return "redirect:" + normalizarRetorno(returnTo);
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("propiedadForm") PropiedadAdminFormDto formulario,
            BindingResult resultado,
            @RequestParam(name = "imagenes", required = false) List<MultipartFile> imagenes,
            @RequestParam(name = "imagenesConservar", required = false) List<Long> imagenesConservar,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "returnTo", defaultValue = "/admin/propiedades") String returnTo) {
        List<ImagenPropiedad> imagenesAnteriores = propiedadService.listarImagenes(id);
        List<Long> idsConservar = imagenesConservar == null ? List.of() : imagenesConservar;
        Set<Long> idsConservarUnicos = new HashSet<>(idsConservar);
        long cantidadConservadas = imagenesAnteriores.stream()
                .filter(imagen -> idsConservarUnicos.contains(imagen.getId()))
                .count();

        if (resultado.hasErrors()) {
            model.addAttribute("propiedadId", id);
            model.addAttribute("formAction", "/admin/propiedades/" + id);
            model.addAttribute("returnTo", normalizarRetorno(returnTo));
            cargarAgentes(model);
            cargarImagenes(model, id, idsConservar);
            return "admin/propiedad-form";
        }
        int cantidadNuevas = contarArchivos(imagenes);
        if (cantidadConservadas + cantidadNuevas > PropiedadService.MAX_IMAGENES) {
            resultado.reject("imagenes.limite",
                    "Una propiedad puede tener como máximo " + PropiedadService.MAX_IMAGENES + " imágenes.");
            model.addAttribute("propiedadId", id);
            model.addAttribute("formAction", "/admin/propiedades/" + id);
            model.addAttribute("returnTo", normalizarRetorno(returnTo));
            cargarAgentes(model);
            cargarImagenes(model, id, idsConservar);
            return "admin/propiedad-form";
        }

        List<String> rutasImagenes;
        try {
            rutasImagenes = imagenStorageService.guardar(
                    imagenes, PropiedadService.MAX_IMAGENES - (int) cantidadConservadas);
        } catch (ImagenInvalidaException excepcion) {
            resultado.reject("imagenes.invalidas", excepcion.getMessage());
            model.addAttribute("propiedadId", id);
            model.addAttribute("formAction", "/admin/propiedades/" + id);
            cargarAgentes(model);
            cargarImagenes(model, id, idsConservar);
            return "admin/propiedad-form";
        }
        List<String> rutasAEliminar = imagenesAnteriores.stream()
                .filter(imagen -> !idsConservarUnicos.contains(imagen.getId()))
                .map(ImagenPropiedad::getUrl)
                .toList();
        try {
            propiedadService.guardar(id, formulario, rutasImagenes, idsConservar);
        } catch (RuntimeException excepcion) {
            imagenStorageService.eliminar(rutasImagenes);
            throw excepcion;
        }
        imagenStorageService.eliminar(rutasAEliminar);
        redirectAttributes.addFlashAttribute("mensaje", "Propiedad actualizada correctamente.");
        return "redirect:" + normalizarRetorno(returnTo);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        propiedadService.eliminar(id);
        redirectAttributes.addFlashAttribute(
                "mensaje", "Propiedad retirada del catálogo; se conserva para el historial.");
        return "redirect:/admin/propiedades";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        propiedadService.reactivar(id);
        redirectAttributes.addFlashAttribute(
                "mensaje", "Propiedad reactivada y publicada en el catálogo.");
        return "redirect:/admin/propiedades?estado=TODAS";
    }

    private void cargarAgentes(Model model) {
        model.addAttribute("agentes", agenteService.listarTodos());
    }

    private void cargarImagenes(Model model, Long id, List<Long> idsSeleccionados) {
        List<ImagenPropiedad> imagenesGuardadas = id == null
                ? List.of()
                : propiedadService.listarImagenes(id);
        model.addAttribute("imagenesGuardadas", imagenesGuardadas);
        model.addAttribute("imagenesConservar", idsSeleccionados == null
                ? imagenesGuardadas.stream().map(ImagenPropiedad::getId).toList()
                : idsSeleccionados);
    }

    private int contarArchivos(List<MultipartFile> archivos) {
        return archivos == null
                ? 0
                : (int) archivos.stream()
                        .filter(archivo -> archivo != null && !archivo.isEmpty())
                        .count();
    }

    private String normalizarRetorno(String returnTo) {
        return "/admin".equals(returnTo) ? "/admin" : "/admin/propiedades";
    }
}
