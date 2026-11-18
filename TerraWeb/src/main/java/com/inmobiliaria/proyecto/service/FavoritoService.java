package com.inmobiliaria.proyecto.service;

import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.Usuario;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.UsuarioRepository;

@Service
public class FavoritoService {

    private final UsuarioRepository usuarioRepository;
    private final PropiedadRepository propiedadRepository;

    public FavoritoService(UsuarioRepository usuarioRepository, PropiedadRepository propiedadRepository) {
        this.usuarioRepository = usuarioRepository;
        this.propiedadRepository = propiedadRepository;
    }

    @Transactional(readOnly = true)
    public Set<Long> listar(String correo) {
        return Set.copyOf(obtenerUsuario(correo).getPropiedadesFavoritas());
    }

    @Transactional
    public Set<Long> agregar(String correo, Long propiedadId) {
        Propiedad propiedad = propiedadRepository.findByIdAndPublicadaTrue(propiedadId)
                .filter(item -> esEstadoPublico(item.getEstado()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró una propiedad disponible con el identificador " + propiedadId));
        Usuario usuario = obtenerUsuario(correo);
        usuario.getPropiedadesFavoritas().add(propiedad.getId());
        usuarioRepository.save(usuario);
        return Set.copyOf(usuario.getPropiedadesFavoritas());
    }

    @Transactional
    public Set<Long> quitar(String correo, Long propiedadId) {
        Usuario usuario = obtenerUsuario(correo);
        usuario.getPropiedadesFavoritas().remove(propiedadId);
        usuarioRepository.save(usuario);
        return Set.copyOf(usuario.getPropiedadesFavoritas());
    }

    private Usuario obtenerUsuario(String correo) {
        return usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta activa."));
    }

    private boolean esEstadoPublico(String estado) {
        return "DISPONIBLE".equalsIgnoreCase(Objects.toString(estado, ""))
                || "RESERVADA".equalsIgnoreCase(Objects.toString(estado, ""));
    }
}
