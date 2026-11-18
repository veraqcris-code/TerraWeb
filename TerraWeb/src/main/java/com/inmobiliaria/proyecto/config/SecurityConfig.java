package com.inmobiliaria.proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
public class SecurityConfig {

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
                                "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST, "/propiedades/*/visitas",
                                "/api/v1/solicitudes-visita").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/inmuebles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/inmuebles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/inmuebles/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/mi-cuenta/favoritos", "/api/v1/mi-cuenta/favoritos/**")
                        .hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/mi-cuenta", "/mi-cuenta/**").hasRole("CLIENTE")
                        .anyRequest().permitAll())
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
