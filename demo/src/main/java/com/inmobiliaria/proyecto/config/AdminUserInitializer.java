package com.inmobiliaria.proyecto.config;

import java.math.BigDecimal;

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
import com.inmobiliaria.proyecto.repository.TestimonioRepository;
import com.inmobiliaria.proyecto.repository.UsuarioRepository; 

@Configuration
public class AdminUserInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Bean
    ApplicationRunner provisionarAdmin(
            UsuarioRepository usuarioRepository,
            PropiedadRepository propiedadRepository, 
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

            if (propiedadRepository.count() == 0) {
                
                // ==================== INMUEBLE DE PRUEBA 1 ====================
                Propiedad casa1 = new Propiedad();
                casa1.setTitulo("Hermosa residencia urbana de estreno");
                casa1.setPrecio(new BigDecimal("1250000"));
                casa1.setMoneda("PEN");
                casa1.setModalidad("VENTA"); 
                casa1.setDireccion("Calle Alcanfores, Miraflores, Lima");
                casa1.setHabitaciones(3);
                casa1.setBanos(4);
                casa1.setAreaConstruida(new BigDecimal("285.0")); 
                casa1.setAreaTotal(new BigDecimal("300.0"));
                casa1.setDescripcion("Una casa urbana moderna contemporánea de diseño minimalista.");

                ImagenPropiedad img1 = new ImagenPropiedad();
                img1.setUrl("inmueble1.jpg"); // Apunta a tu primera imagen física
                img1.setOrden(1);
                casa1.agregarImagen(img1); 

                propiedadRepository.save(casa1);

                // ==================== INMUEBLE DE PRUEBA 2 ====================
                Propiedad casa2 = new Propiedad();
                casa2.setTitulo("Moderna casa minimalista con jardín");
                casa2.setPrecio(new BigDecimal("10000"));
                casa2.setMoneda("PEN");
                casa2.setModalidad("VENTA"); 
                casa2.setDireccion("Los Almendros, Castilla, Piura");
                casa2.setHabitaciones(2);
                casa2.setBanos(2);
                casa2.setAreaConstruida(new BigDecimal("250.0")); 
                casa2.setAreaTotal(new BigDecimal("280.0"));
                casa2.setDescripcion("Hermoso inmueble con amplias áreas verdes y excelente iluminación.");

                ImagenPropiedad img2 = new ImagenPropiedad();
                img2.setUrl("inmueble2.jpg"); // Apunta a tu segunda imagen física sin duplicados
                img2.setOrden(1);
                casa2.agregarImagen(img2); 

                propiedadRepository.save(casa2); 
                
                LOGGER.info("Base de datos H2 vacía. Se crearon exitosamente los 2 inmuebles de prueba únicos.");

                // ==================== INMUEBLE DE PRUEBA 3 ====================
                Propiedad casa3 = new Propiedad();
                casa3.setTitulo("Moderna casa minimalista con jardín");
                casa3.setPrecio(new BigDecimal("10000"));
                casa3.setMoneda("PEN");
                casa3.setModalidad("VENTA"); 
                casa3.setDireccion("Los Almendros, Castilla, Piura");
                casa3.setHabitaciones(2);
                casa3.setBanos(2);
                casa3.setAreaConstruida(new BigDecimal("250.0")); 
                casa3.setAreaTotal(new BigDecimal("280.0"));
                casa3.setDescripcion("Hermoso inmueble con amplias áreas verdes y excelente iluminación.");

                ImagenPropiedad img3 = new ImagenPropiedad();
                img3.setUrl("inmueble3.webp"); // Apunta a tu tercera imagen física sin duplicados
                img3.setOrden(1);
                casa3.agregarImagen(img3); 

                propiedadRepository.save(casa3); 
                
                LOGGER.info("Base de datos H2 vacía. Se crearon exitosamente los 3 inmuebles de prueba únicos.");
            }
            


            if (testimonioRepository.count() == 0) {
                // Testimonio 1
                Testimonio t1 = new Testimonio();
                t1.setNombre("Carlos Mendoza");
                t1.setUbicacion("San Isidro, Lima");
                t1.setTitulo("Excelente servicio y rapidez");
                t1.setContenido("Logré vender mi departamento en tiempo récord y a un precio justo. El trato del equipo de P&D fue muy profesional de principio a fin.");
                t1.setPuntuacion(5);
                t1.setActivo(true);
                testimonioRepository.save(t1);

                // Testimonio 2
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
}
