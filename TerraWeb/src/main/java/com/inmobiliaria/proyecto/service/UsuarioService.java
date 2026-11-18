package com.inmobiliaria.proyecto.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.usuario.RegistroClienteDto;
import com.inmobiliaria.proyecto.dto.usuario.PerfilUsuarioDto;
import com.inmobiliaria.proyecto.exception.CorreoEnUsoException;
import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.models.RolUsuario;
import com.inmobiliaria.proyecto.models.Usuario;
import com.inmobiliaria.proyecto.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registrarCliente(RegistroClienteDto datos) {
        String correo = datos.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new CorreoEnUsoException("Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(datos.getTelefono().trim());
        usuario.setPasswordHash(passwordEncoder.encode(datos.getPassword()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setDocumentoIdentidad(null);
        usuario.setResidencia(null);
        usuario.setMonedaPreferida("PEN");
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public PerfilUsuarioDto obtenerPerfil(String correo) {
        return usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correo)
                .map(PerfilUsuarioDto::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta activa."));
    }

    @Transactional
    public PerfilUsuarioDto actualizarPerfil(String correoActual, PerfilUsuarioDto datos) {
        return actualizarPerfil(correoActual, datos, null);
    }

    @Transactional
    public PerfilUsuarioDto actualizarPerfil(String correoActual, PerfilUsuarioDto datos, String fotoPerfilUrl) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correoActual)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta activa."));
        String correoNuevo = datos.getCorreo().trim().toLowerCase();
        if (!usuario.getCorreo().equalsIgnoreCase(correoNuevo)
                && usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correoNuevo, usuario.getId())) {
            throw new CorreoEnUsoException("Ya existe una cuenta con ese correo.");
        }

        usuario.setNombre(datos.getNombre().trim());
        usuario.setCorreo(correoNuevo);
        usuario.setTelefono(datos.getTelefono().trim());
        usuario.setDocumentoIdentidad(normalizarOpcional(datos.getDocumentoIdentidad()));
        usuario.setResidencia(normalizarOpcional(datos.getResidencia()));
        usuario.setMonedaPreferida(datos.getMonedaPreferida());
        if (fotoPerfilUrl != null && !fotoPerfilUrl.isBlank()) {
            usuario.setFotoPerfilUrl(fotoPerfilUrl);
        }
        usuarioRepository.save(usuario);
        return PerfilUsuarioDto.desde(usuario);
    }

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param correo the user's login email
     * @param passwordActual the current raw password
     * @param passwordNueva the new raw password
     * @return {@code true} when the password was changed
     */
    @Transactional
    public boolean cambiarPassword(String correo, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta activa."));
        if (usuario.getPasswordHash() == null
                || !passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            return false;
        }
        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        return true;
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
