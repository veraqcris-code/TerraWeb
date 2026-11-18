package com.inmobiliaria.proyecto.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.inmobiliaria.proyecto.models.ImagenPropiedad;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.RolUsuario;
import com.inmobiliaria.proyecto.models.Testimonio;
import com.inmobiliaria.proyecto.models.Usuario;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.SolicitudVisitaRepository;
import com.inmobiliaria.proyecto.repository.TestimonioRepository;
import com.inmobiliaria.proyecto.repository.UsuarioRepository;

@Configuration
public class AdminUserInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Bean
    ApplicationRunner provisionarAdmin(
            UsuarioRepository usuarioRepository,
            PropiedadRepository propiedadRepository,
            SolicitudVisitaRepository solicitudVisitaRepository,
            TestimonioRepository testimonioRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.core.env.Environment environment) {
        return args -> {
            
            String correo = environment.getProperty("app.admin.email", "").trim();
            String password = environment.getProperty("app.admin.password", "");
            if (correo.isBlank() && password.isBlank()) {
                LOGGER.warn("No se configuró ADMIN_EMAIL/ADMIN_PASSWORD; no se creó una cuenta ADMIN inicial.");
            } else if (correo.isBlank() || password.isBlank()) {
                throw new IllegalStateException(
                        "Configure ADMIN_EMAIL y ADMIN_PASSWORD juntos para crear el administrador inicial.");
            } else if (password.length() < 8) {
                throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos 8 caracteres.");
            } else if (!usuarioRepository.existsByCorreoIgnoreCase(correo)) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador P&D");
                admin.setCorreo(correo.toLowerCase());
                admin.setPasswordHash(passwordEncoder.encode(password));
                admin.setRol(RolUsuario.ADMIN);
                usuarioRepository.save(admin);
                LOGGER.info("Se creó la cuenta ADMIN inicial para {}", correo);
            }

            eliminarPropiedadesDePrueba(propiedadRepository, solicitudVisitaRepository);

            if (testimonioRepository.count() == 0) {
                Testimonio t1 = new Testimonio();
                t1.setNombre("Carlos Mendoza");
                t1.setUbicacion("San Isidro, Lima");
                t1.setTitulo("Excelente servicio y rapidez");
                t1.setContenido("Logré vender mi departamento en tiempo récord y a un precio justo. El trato del equipo de P&D fue muy profesional de principio a fin.");
                t1.setPuntuacion(5);
                t1.setActivo(true);
                testimonioRepository.save(t1);

                Testimonio t2 = new Testimonio();
                t2.setNombre("Ana María Rossi");
                t2.setUbicacion("Miraflores, Lima");
                t2.setTitulo("Altamente recomendados");
                t2.setContenido("El diseño del sistema y el acompañamiento de los agentes inmobiliarios fue impecable. Brindan mucha confianza en todo el proceso.");
                t2.setPuntuacion(5);
                t2.setActivo(true);
                testimonioRepository.save(t2);

                LOGGER.info("Base de datos H2 vacía. Se crearon testimonios de prueba utilizando los atributos reales del modelo.");
            }
        };
    }

    private void eliminarPropiedadesDePrueba(
            PropiedadRepository propiedadRepository,
            SolicitudVisitaRepository solicitudVisitaRepository) {
        propiedadRepository.findByTituloIn(List.of(
                        "Hermosa residencia urbana de estreno",
                        "Moderna casa minimalista con jardín"))
                .stream()
                .filter(this::esPropiedadDePrueba)
                .forEach(propiedad -> {
                    solicitudVisitaRepository.desvincularPropiedad(propiedad.getId());
                    propiedadRepository.delete(propiedad);
                    LOGGER.info("Se eliminó una propiedad precargada de prueba: {}", propiedad.getTitulo());
                });
    }

    private boolean esPropiedadDePrueba(Propiedad propiedad) {
        String titulo = propiedad.getTitulo();
        String direccion = propiedad.getDireccion();
        String imagen = propiedad.getImagenes().stream()
                .map(ImagenPropiedad::getUrl)
                .findFirst()
                .orElse("");
        return ("Hermosa residencia urbana de estreno".equals(titulo)
                        && "Calle Alcanfores, Miraflores, Lima".equals(direccion)
                        && "inmueble1.jpg".equals(imagen))
                || ("Moderna casa minimalista con jardín".equals(titulo)
                        && "Los Almendros, Castilla, Piura".equals(direccion)
                        && ("inmueble2.jpg".equals(imagen) || "inmueble3.webp".equals(imagen)));
    }
}
