---
sessionId: session-261116-001106-qvs0
---

# Auditoría General

### 📋 Resumen del Proyecto

**TerraWeb** es una aplicación web inmobiliaria desarrollada con **Spring Boot 4.1.1** y **Java 21**. Utiliza **MySQL** como base de datos, **Thymeleaf** para las vistas, **Spring Security** para autenticación, y **Tailwind CSS** (vía CDN) para el diseño frontend. El proyecto cuenta con **74 archivos Java**, **17 plantillas HTML**, y **4 archivos CSS** personalizados.

#### Arquitectura General
- **Patrón**: Arquitectura en capas (Controller → Service → Repository)
- **Paquete base**: `com.inmobiliaria.proyecto`
- **Capas**: `config`, `controller`, `dto`, `exception`, `mapper`, `models`, `repository`, `security`, `service`
- **Roles**: `ADMIN` y `CLIENTE`
- **API REST**: Endpoints en `/api/v1/**` con respuestas JSON
- **Panel Admin**: Vistas Thymeleaf en `/admin/**`

---

### 🔴 Hallazgos Críticos (Seguridad)

#### 1. Credenciales hardcodeadas en `application.properties`
```properties
spring.datasource.username=root
spring.datasource.password=soldepiura
```
**Riesgo**: Las credenciales de la base de datos están expuestas en el código fuente. Si el repositorio se comparte o se compromete, cualquier persona puede acceder a la base de datos.
**Recomendación**: Usar variables de entorno o un archivo `.env` excluido del control de versiones. Spring Boot ya soporta `${DB_USER:}` y `${DB_PASSWORD:}`.

#### 2. CSRF habilitado sin protección en formularios públicos
Los formularios de **login.html** y **registro.html** realizan `POST` sin incluir tokens CSRF. Aunque Spring Security ignora CSRF para `GET`, los endpoints `POST /login` y `POST /registro` están expuestos a ataques CSRF.
**Recomendación**: Agregar tokens CSRF en los formularios de login y registro, o deshabilitar CSRF explícitamente para los endpoints de autenticación si se usa un enfoque alternativo.

#### 3. Contraseña mínima de 12 caracteres en registro, pero sin validación de complejidad
`RegistroClienteDto` exige `@Size(min = 12, max = 72)` para la contraseña, pero no valida mayúsculas, números ni caracteres especiales.
**Recomendación**: Agregar una restricción `@Pattern` que requiera al menos una mayúscula, un número y un carácter especial.

#### 4. `rememberMe` sin clave configurada
En `SecurityConfig.java` se usa `.rememberMe(Customizer.withDefaults())` sin configurar una clave de firma segura.
**Recomendación**: Configurar una clave `rememberMe.key()` de al menos 8 caracteres o usar `PersistentTokenBasedRememberMeServices`.

---

### 🟡 Hallazgos Moderados (Calidad y Buenas Prácticas)

#### 5. Modelo `Inmueble` duplicado/obsoleto
Existe un modelo `Inmueble.java` con solo 3 campos (titulo, precio, descripcion) y un `InmuebleController.java` con comentarios de tema educativo. Parece código de prueba/demo que coexiste con el modelo `Propiedad` completo.
**Recomendación**: Eliminar o integrar el modelo `Inmueble` si ya no se usa, ya que genera confusión y duplica funcionalidad.

#### 6. Exposición de SQL en producción
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
**Riesgo**: En producción, esto genera logs verbosos con consultas SQL que pueden exponer datos sensibles y afectar el rendimiento.
**Recomendación**: Usar perfiles de Spring Boot (`application-dev.properties`, `application-prod.properties`) para activar esto solo en desarrollo.

#### 7. `open-in-view=false` configurado correctamente pero sin verificación
La configuración `spring.jpa.open-in-view=false` es correcta y evita el problema de SessionInView. Sin embargo, algunas consultas lazy (como `Propiedad.agente`) podrían fallar si se acceden fuera de una transacción.
**Recomendación**: Verificar que todos los controllers que acceden a relaciones lazy lo hagan dentro de un contexto transaccional o usar `@EntityGraph`.

#### 8. Validación de contraseña en `cambiarPassword` sin DTO validado
El endpoint `POST /api/v1/admin/configuracion/password` recibe `CambiarPasswordAdminDto` con `@Valid`, pero la validación de coincidencia de contraseñas se hace manualmente en el controller.
**Recomendación**: Mover la validación de coincidencia al DTO con `@AssertTrue`.

---

### 🟢 Puntos Fuertes del Proyecto

#### 9. Estructura de capas bien definida
El proyecto sigue un patrón claro de separación de responsabilidades:
- Controllers manejan solo HTTP y delegan a servicios
- Servicios contienen la lógica de negocio
- Repositorios manejan acceso a datos
- DTOs separan las entidades de persistencia de las respuestas API

#### 10. Gestión segura de contraseñas
- Uso de `BCryptPasswordEncoder` para hash de contraseñas
- Normalización de correos (`trim().toLowerCase()`)
- Verificación de contraseña actual antes de cambios

#### 11. Protección contra path traversal en uploads
`ImagenStorageService.java` incluye validaciones robustas:
- Verificación de firma de archivo (no solo extensión)
- Prevención de path traversal con `normalize()` y validación de directorio padre
- Limpieza de archivos en caso de errores

#### 12. Soft delete en propiedades
`PropiedadService.eliminar()` realiza un soft delete (cambia estado a `RETIRADA`) en lugar de eliminar físicamente, lo que preserva la integridad referencial.

#### 13. CSRF protegido en panel admin
Las plantillas del panel admin (`resumen.html`, `solicitud.html`, `configuracion.html`, `agente.html`) incluyen tokens CSRF correctamente en las peticiones AJAX.

#### 14. Pruebas unitarias con Mockito
`ServiciosPrincipalesUnitTest.java` cubre escenarios clave:
- Registro de clientes con normalización
- Detección de correos duplicados
- Cambio de contraseña con verificación
- Gestión de imágenes (límite de 10)
- Soft delete de propiedades
- Registro de solicitudes de visita

#### 15. EntityGraph para evitar N+1
`PropiedadRepository` usa `@EntityGraph` para cargar imágenes y agente en una sola consulta, evitando el problema N+1.

---

### 🔵 Hallazgos Menores (Mejoras)

#### 16. Falta de `.gitignore` para credenciales
No se evidencia un archivo `.gitignore` que excluya archivos de configuración sensibles.

#### 17. `borrador.html` sin controller asociado
La plantilla `borrador.html` existe en templates pero no tiene un controller que la sirva.

#### 18. Validación de teléfono con regex mejorable
El patrón `^[+0-9() .-]{7,25}$` permite combinaciones inválidas como `+++++` o `.....`.

#### 19. Falta de rate limiting en endpoints de autenticación
No hay protección contra fuerza bruta en `/login` o `/registro`.

#### 20. Dependencias de prueba con nombres incorrectos
`spring-boot-starter-data-jpa-test` y `spring-boot-starter-webmvc-test` no son starters oficiales de Spring Boot. Los correctos son `spring-boot-starter-test` que incluye JUnit, Mockito, y los testers de Spring.

---

### 📊 Resumen de Hallazgos

| Categoría | Crítico | Moderado | Menor | Fuerte |
|-----------|---------|----------|-------|--------|
| Seguridad | 4 | 1 | 2 | 2 |
| Calidad de Código | - | 3 | 2 | 3 |
| Arquitectura | - | 1 | - | 2 |
| Testing | - | - | - | 1 |
| Configuración | 1 | 1 | 1 | - |

**Total**: 4 críticos, 6 moderados, 5 menores, 8 puntos fuertes

# Delivery Steps

### * Step 1: Auditoría completada
Esta es una auditoría estática sin implementación. Se han identificado 15 hallazgos organizados por prioridad: 4 críticos de seguridad, 6 moderados de calidad, y 5 menores de mejora. El proyecto tiene una base sólida con buenas prácticas en capas, seguridad de contraseñas, y protección CSRF en el panel admin.