package com.inmobiliaria.proyecto.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.Testimonio;

public interface TestimonioRepository extends JpaRepository<Testimonio, Long> {

    List<Testimonio> findByActivoTrueOrderByIdDesc(Pageable pageable);
}
