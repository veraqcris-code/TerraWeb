package com.inmobiliaria.proyecto.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.usuario.RegistroClienteDto;
import com.inmobiliaria.proyecto.exception.CorreoEnUsoException;
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
        usuario.setPasswordHash(passwordEncoder.encode(datos.getPassword()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuarioRepository.save(usuario);
    }
}
