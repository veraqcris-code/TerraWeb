package com.inmobiliaria.proyecto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCaseAndActivoTrue(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
