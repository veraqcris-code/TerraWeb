package com.inmobiliaria.proyecto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.inmobiliaria.proyecto.dto.usuario.RegistroClienteDto;
import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaDto;
import com.inmobiliaria.proyecto.exception.CorreoEnUsoException;
import com.inmobiliaria.proyecto.mapper.PropiedadMapper;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.RolUsuario;
import com.inmobiliaria.proyecto.models.SolicitudVisita;
import com.inmobiliaria.proyecto.models.Usuario;
import com.inmobiliaria.proyecto.repository.AgenteRepository;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.SolicitudVisitaRepository;
import com.inmobiliaria.proyecto.repository.UsuarioRepository;

class ServiciosPrincipalesUnitTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private PropiedadRepository propiedadRepository;
    private AgenteRepository agenteRepository;
    private SolicitudVisitaRepository solicitudVisitaRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        propiedadRepository = org.mockito.Mockito.mock(PropiedadRepository.class);
        agenteRepository = org.mockito.Mockito.mock(AgenteRepository.class);
        solicitudVisitaRepository = org.mockito.Mockito.mock(SolicitudVisitaRepository.class);
    }

    @Test
    void registroGuardaClienteConCorreoNormalizadoRolSeguroYPasswordHasheada() {
        UsuarioService servicio = new UsuarioService(usuarioRepository, passwordEncoder);
        RegistroClienteDto datos = new RegistroClienteDto();
        datos.setNombre("  Ana Pérez  ");
        datos.setCorreo("  ANA@EXAMPLE.COM ");
        datos.setPassword("ClaveSegura123!");
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123!")).thenReturn("hash-seguro");

        servicio.registrarCliente(datos);

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("Ana Pérez", guardado.getNombre());
        assertEquals("ana@example.com", guardado.getCorreo());
        assertEquals(RolUsuario.CLIENTE, guardado.getRol());
        assertEquals("hash-seguro", guardado.getPasswordHash());
    }

    @Test
    void registroRechazaCorreoDuplicadoSinGuardarUsuarioNiCodificarPassword() {
        UsuarioService servicio = new UsuarioService(usuarioRepository, passwordEncoder);
        RegistroClienteDto datos = new RegistroClienteDto();
        datos.setNombre("Ana Pérez");
        datos.setCorreo("ana@example.com");
        datos.setPassword("ClaveSegura123!");
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@example.com")).thenReturn(true);

        assertThrows(CorreoEnUsoException.class, () -> servicio.registrarCliente(datos));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void catalogoDevuelveResumenesYLimitaLaConsultaAMaximoCien() {
        Propiedad propiedad = new Propiedad();
        propiedad.setId(7L);
        propiedad.setTitulo("Casa familiar");
        propiedad.setPrecio(new BigDecimal("250000"));
        propiedad.setMoneda("PEN");
        propiedad.setDireccion("Av. Central");
        propiedad.setDistrito("Miraflores");
        propiedad.setCiudad("Lima");
        when(propiedadRepository.findByPublicadaTrueOrderByFechaPublicacionDesc(PageRequest.of(0, 100)))
                .thenReturn(List.of(propiedad));
        PropiedadService servicio = new PropiedadService(
                propiedadRepository, new PropiedadMapper(), agenteRepository);

        var resultados = servicio.listarRecientes(500);

        assertEquals(1, resultados.size());
        assertEquals(7L, resultados.get(0).id());
        assertEquals("Casa familiar", resultados.get(0).titulo());
        assertEquals("Av. Central, Miraflores, Lima", resultados.get(0).direccionCompleta());
        verify(propiedadRepository)
                .findByPublicadaTrueOrderByFechaPublicacionDesc(PageRequest.of(0, 100));
    }

    @Test
    void eliminarPropiedadLaRetiraSinBorrarlaFisicamente() {
        Propiedad propiedad = new Propiedad();
        propiedad.setId(12L);
        propiedad.setPublicada(true);
        propiedad.setEstado("DISPONIBLE");
        when(propiedadRepository.findById(12L)).thenReturn(Optional.of(propiedad));
        when(propiedadRepository.save(propiedad)).thenReturn(propiedad);
        PropiedadService servicio = new PropiedadService(
                propiedadRepository, new PropiedadMapper(), agenteRepository);

        servicio.eliminar(12L);

        assertFalse(propiedad.isPublicada());
        assertEquals("RETIRADA", propiedad.getEstado());
        verify(propiedadRepository).save(propiedad);
        verify(propiedadRepository, never()).delete(any(Propiedad.class));
    }

    @Test
    void solicitudDeVisitaRegistraDatosVinculadosSoloAPropiedadPublicada() {
        Propiedad propiedad = new Propiedad();
        propiedad.setId(21L);
        propiedad.setTitulo("Casa publicada");
        SolicitudVisitaDto datos = new SolicitudVisitaDto();
        datos.setNombre("  Carlos Ruiz ");
        datos.setTelefono(" 999 888 777 ");
        datos.setCorreo(" carlos@example.com ");
        datos.setFechaEstimada(LocalDate.now().plusDays(3));
        datos.setPropiedadId(21L);
        when(propiedadRepository.findByIdAndPublicadaTrue(21L)).thenReturn(Optional.of(propiedad));
        when(solicitudVisitaRepository.save(any(SolicitudVisita.class))).thenAnswer(invocacion -> {
            SolicitudVisita solicitud = invocacion.getArgument(0);
            solicitud.setId(31L);
            return solicitud;
        });
        SolicitudVisitaService servicio = new SolicitudVisitaService(
                solicitudVisitaRepository, propiedadRepository);

        var respuesta = servicio.registrar(21L, datos);

        var captor = org.mockito.ArgumentCaptor.forClass(SolicitudVisita.class);
        verify(solicitudVisitaRepository).save(captor.capture());
        SolicitudVisita guardada = captor.getValue();
        assertEquals("Carlos Ruiz", guardada.getNombre());
        assertEquals("999 888 777", guardada.getTelefono());
        assertEquals("carlos@example.com", guardada.getCorreo());
        assertSame(propiedad, guardada.getPropiedad());
        assertEquals("Casa publicada", guardada.getTituloPropiedad());
        assertEquals(31L, respuesta.id());
        assertEquals(21L, respuesta.propiedadId());
        verify(propiedadRepository).findByIdAndPublicadaTrue(21L);
    }

}
