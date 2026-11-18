package com.inmobiliaria.proyecto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.ConfiguracionAdmin;

public interface ConfiguracionAdminRepository extends JpaRepository<ConfiguracionAdmin, Long> {

    Optional<ConfiguracionAdmin> findFirstByOrderByIdAsc();
}
