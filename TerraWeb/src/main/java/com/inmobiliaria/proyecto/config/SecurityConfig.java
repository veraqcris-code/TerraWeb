package com.inmobiliaria.proyecto.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    private static String jsonError(Instant fecha, int estado, String mensaje) {
        String fechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneId.systemDefault())
                .format(fecha);
        return "{\"fecha\":\"" + fechaIso + "\",\"estado\":" + estado + ",\"mensaje\":\"" + mensaje + "\",\"errores\":{}}";
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository()))
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/registro", "/error").permitAll()
                        .requestMatchers("/vistas.html/admin.html").denyAll()
                        .requestMatchers(HttpMethod.GET, "/", "/propiedades/**", "/api/v1/propiedades/**",
                                "/api/v1/inmuebles/**",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST, "/propiedades/*/visitas",
                                "/api/v1/solicitudes-visita").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/inmuebles/**", "/api/v1/inmuebles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/inmuebles/**", "/api/v1/inmuebles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/inmuebles/**", "/api/v1/inmuebles/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/mi-cuenta/favoritos", "/api/v1/mi-cuenta/favoritos/**")
                        .hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers("/api/v1/mi-cuenta", "/api/v1/mi-cuenta/**")
                        .hasRole("CLIENTE")
                        .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/mi-cuenta", "/mi-cuenta/**").hasRole("CLIENTE")
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            try {
                                response.getWriter().write(jsonError(Instant.now(), 401, "Se requiere iniciar sesión"));
                            } catch (java.io.IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            try {
                                response.getWriter().write(jsonError(Instant.now(), 403, "No tienes permiso para esta operación"));
                            } catch (java.io.IOException e) {
                                throw new RuntimeException(e);
                            }
                        }))
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout"))
                .rememberMe(Customizer.withDefaults())
                .build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean administrador = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(administrador ? "/admin" : "/");
        };
    }
}
