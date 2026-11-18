package com.inmobiliaria.proyecto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.ImagenPropiedad;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.RolUsuario;
import com.inmobiliaria.proyecto.models.SolicitudVisita;
import com.inmobiliaria.proyecto.models.Usuario;
import com.inmobiliaria.proyecto.repository.AgenteRepository;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.SolicitudVisitaRepository;
import com.inmobiliaria.proyecto.repository.UsuarioRepository;
import com.inmobiliaria.proyecto.service.ImagenStorageService;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
		"app.admin.email=admin-test@terraweb.local",
		"app.admin.password=admin123"
})
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PropiedadRepository propiedadRepository;

	@Autowired
	private AgenteRepository agenteRepository;

	@Autowired
	private SolicitudVisitaRepository solicitudVisitaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ImagenStorageService imagenStorageService;

	@Test
	void contextLoads() {
	}

	@Test
	void inicioRenderizaLaPlantillaThymeleaf() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("inicio"))
				.andExpect(MockMvcResultMatchers.content().string(
						org.hamcrest.Matchers.containsString("Últimas")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/mi-cuenta\"")));
	}

	@Test
	void inicioCargaLaImagenDePruebaDesdeStaticImages() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Residencia de prueba con imagen");
		propiedad.setPrecio(new BigDecimal("1250000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		propiedad.setFechaPublicacion(LocalDate.now().plusDays(1));

		ImagenPropiedad imagen = new ImagenPropiedad();
		imagen.setUrl("/images/inmueble1.jpg");
		imagen.setTextoAlternativo("Imagen de prueba inmueble 1");
		imagen.setEtiqueta("Fachada");
		imagen.setOrden(1);
		propiedad.agregarImagen(imagen);
		propiedadRepository.saveAndFlush(propiedad);

		mockMvc.perform(get("/images/{nombre}", "inmueble1.jpg"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("image/jpeg"));

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("src=\"/images/inmueble1.jpg\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("alt=\"Imagen de prueba inmueble 1\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(
								"En este momento no hay propiedades publicadas."))));

		Propiedad propiedadSubida = new Propiedad();
		propiedadSubida.setTitulo("Residencia de prueba con imagen subida");
		propiedadSubida.setPrecio(new BigDecimal("980000"));
		propiedadSubida.setMoneda("PEN");
		propiedadSubida.setModalidad("Venta");
		propiedadSubida.setFechaPublicacion(LocalDate.now().plusDays(2));
		ImagenPropiedad portadaSubida = new ImagenPropiedad();
		portadaSubida.setUrl("/uploads/portada-prueba.png");
		portadaSubida.setOrden(0);
		propiedadSubida.agregarImagen(portadaSubida);
		propiedadRepository.saveAndFlush(propiedadSubida);

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("src=\"/uploads/portada-prueba.png\"")));
	}

	@Test
	void detalleRenderizaLosDatosDeUnaPropiedad() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Casa de prueba");
		propiedad.setPrecio(new BigDecimal("1250000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		Propiedad guardada = propiedadRepository.saveAndFlush(propiedad);

		mockMvc.perform(get("/propiedades/{id}", guardada.getId()))
				.andExpect(status().isOk())
				.andExpect(view().name("detallePropiedad"))
				.andExpect(MockMvcResultMatchers.content().string(
						org.hamcrest.Matchers.containsString("Casa de prueba")));
	}

	@Test
	void apiEntregaElDetalleEnFormatoJson() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Casa para API");
		propiedad.setPrecio(new BigDecimal("850000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		Propiedad guardada = propiedadRepository.saveAndFlush(propiedad);

		mockMvc.perform(get("/api/v1/propiedades/{id}", guardada.getId()))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.titulo").value("Casa para API"));
	}

	@Test
	void apiRegistraUnaSolicitudDeVisitaValidada() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Casa para visita");
		propiedad.setPrecio(new BigDecimal("650000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		Propiedad guardada = propiedadRepository.saveAndFlush(propiedad);
		String fechaEstimada = LocalDate.now().plusDays(2).toString();
		String cuerpo = """
				{
				  "propiedadId": %d,
				  "nombre": "Ana Pérez",
				  "telefono": "+51 987 654 321",
				  "correo": "ana@example.com",
				  "fechaEstimada": "%s"
				}
				""".formatted(guardada.getId(), fechaEstimada);

		mockMvc.perform(post("/api/v1/solicitudes-visita")
						.with(csrf())
						.contentType(APPLICATION_JSON)
						.content(cuerpo))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.propiedadId").value(guardada.getId()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.mensaje").isNotEmpty());
	}

	@Test
	void adminRequiereAutenticacionYLoginRedirigeSegunElRol() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));

		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"username\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"remember-me\"")));

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", "admin-test@terraweb.local")
						.param("password", "admin123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "/admin"));
	}

	@Test
	void registroPublicoSiempreCreaUnClienteAunqueSeEnvieUnRol() throws Exception {
		String correoRegistro = "cliente-" + UUID.randomUUID() + "@terraweb.local";
		mockMvc.perform(get("/registro"))
				.andExpect(status().isOk())
				.andExpect(view().name("registro"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"telefono\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"aceptaTerminos\"")));

		mockMvc.perform(post("/registro")
						.with(csrf())
						.param("nombre", "Cliente de prueba")
						.param("correo", correoRegistro)
						.param("telefono", "+51 987 654 321")
						.param("password", "ClienteSeguro123!")
						.param("confirmarPassword", "ClienteSeguro123!")
						.param("aceptaTerminos", "true")
						.param("rol", "ADMIN"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));

		Usuario usuario = usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correoRegistro)
				.orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(RolUsuario.CLIENTE, usuario.getRol());
		org.junit.jupiter.api.Assertions.assertEquals("+51 987 654 321", usuario.getTelefono());
		org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches(
				"ClienteSeguro123!", usuario.getPasswordHash()));

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", correoRegistro)
						.param("password", "ClienteSeguro123!"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "/"));
	}

	@Test
	void perfilCargaLosDatosRealesYGuardaLosCambiosEnLaCuenta() throws Exception {
		String correoOriginal = "perfil-" + UUID.randomUUID() + "@terraweb.local";
		String correoActualizado = "perfil-actualizado-" + UUID.randomUUID() + "@terraweb.local";
		Usuario usuario = new Usuario();
		usuario.setNombre("María Prueba");
		usuario.setCorreo(correoOriginal);
		usuario.setPasswordHash(passwordEncoder.encode("ClavePerfil123!"));
		usuario.setTelefono("+51 987111222");
		usuario.setRol(RolUsuario.CLIENTE);
		usuarioRepository.saveAndFlush(usuario);

		mockMvc.perform(get("/mi-cuenta")
						.with(user(correoOriginal).roles("CLIENTE")))
				.andExpect(status().isOk())
				.andExpect(view().name("perfil"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("María Prueba")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString(correoOriginal)))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("+51 987111222")));

		var resultado = mockMvc.perform(post("/mi-cuenta")
						.with(user(correoOriginal).roles("CLIENTE"))
						.with(csrf())
						.param("nombre", "María Actualizada")
						.param("correo", correoActualizado)
						.param("telefono", "+51 987 333 444")
						.param("documentoIdentidad", "12345678")
						.param("residencia", "Lima, Perú")
						.param("monedaPreferida", "USD"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/mi-cuenta"))
				.andReturn();

		Usuario actualizado = usuarioRepository
				.findByCorreoIgnoreCaseAndActivoTrue(correoActualizado)
				.orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals("María Actualizada", actualizado.getNombre());
		org.junit.jupiter.api.Assertions.assertEquals("+51 987 333 444", actualizado.getTelefono());
		org.junit.jupiter.api.Assertions.assertEquals("12345678", actualizado.getDocumentoIdentidad());
		org.junit.jupiter.api.Assertions.assertEquals("Lima, Perú", actualizado.getResidencia());
		org.junit.jupiter.api.Assertions.assertEquals("USD", actualizado.getMonedaPreferida());

		MockHttpSession session = (MockHttpSession) resultado.getRequest().getSession(false);
		mockMvc.perform(get("/mi-cuenta").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString(correoActualizado)));
	}

	@Test
	void perfilGuardaYRenderizaLaFotoSubida() throws Exception {
		String correo = "perfil-foto-" + UUID.randomUUID() + "@terraweb.local";
		Usuario usuario = new Usuario();
		usuario.setNombre("Cliente con foto");
		usuario.setCorreo(correo);
		usuario.setPasswordHash(passwordEncoder.encode("ClavePerfil123!"));
		usuario.setTelefono("+51 987111222");
		usuario.setRol(RolUsuario.CLIENTE);
		usuarioRepository.saveAndFlush(usuario);

		MockMultipartFile foto = new MockMultipartFile(
				"foto",
				"perfil.png",
				"image/png",
				new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0, 0, 0, 0});
		mockMvc.perform(multipart("/mi-cuenta")
						.file(foto)
						.with(user(correo).roles("CLIENTE"))
						.with(csrf())
						.param("nombre", "Cliente con foto")
						.param("correo", correo)
						.param("telefono", "+51 987111222")
						.param("monedaPreferida", "PEN"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/mi-cuenta"));

		Usuario actualizado = usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correo).orElseThrow();
		org.junit.jupiter.api.Assertions.assertTrue(actualizado.getFotoPerfilUrl().startsWith("/uploads/"));
		mockMvc.perform(get("/mi-cuenta").with(user(correo).roles("CLIENTE")))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("src=\"" + actualizado.getFotoPerfilUrl() + "\"")));
		imagenStorageService.eliminar(List.of(actualizado.getFotoPerfilUrl()));
	}

	@Test
	void perfilRequiereRolCliente() throws Exception {
		mockMvc.perform(get("/mi-cuenta")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isForbidden());
	}

	@Test
	void usuarioClienteNoPuedeEntrarAlPanelAdmin() throws Exception {
		mockMvc.perform(get("/admin")
						.with(user("cliente@terraweb.local").roles("CLIENTE")))
				.andExpect(status().isForbidden());
	}

	@Test
	void formulariosAdminRenderizanAccionesParaCrearEditarYArchivar() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Propiedad visible en el panel");
		propiedad.setPrecio(new BigDecimal("420000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		propiedad.setFechaPublicacion(LocalDate.now().plusDays(1));
		ImagenPropiedad imagen = new ImagenPropiedad();
		imagen.setUrl("/images/inmueble1.jpg");
		imagen.setOrden(0);
		propiedad.agregarImagen(imagen);
		propiedadRepository.saveAndFlush(propiedad);

		mockMvc.perform(get("/admin")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/resumen"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("Propiedad visible en el panel")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("Solicitudes pendientes")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("Gestionar agentes")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("aria-label=\"Navegación administrativa\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/propiedades\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/solicitudes\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/agentes\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/configuracion\"")));

		mockMvc.perform(get("/admin/propiedades")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/propiedades"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("aria-current=\"page\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/solicitudes\"")));

		mockMvc.perform(get("/admin/solicitudes")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/solicitud"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/propiedades\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/agentes\"")));

		mockMvc.perform(get("/admin/agentes")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/agente"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/solicitudes\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/configuracion\"")));

		mockMvc.perform(get("/admin/configuracion")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/configuracion"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"/admin/agentes\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("aria-current=\"page\"")));

		mockMvc.perform(get("/api/v1/admin/solicitudes")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.startsWith("[")));

		mockMvc.perform(get("/api/v1/admin/agentes")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/admin/configuracion")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("\"nombreEmpresa\"")));

		mockMvc.perform(put("/api/v1/admin/configuracion")
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "nombreEmpresa": "Terraweb Inmobiliaria",
								  "whatsapp": "+51 999 888 777",
								  "correoContacto": "contacto@terraweb.pe",
								  "monedaPrincipal": "PEN",
								  "tipoCambio": 3.75,
								  "idioma": "es-PE",
								  "zonaHoraria": "America/Lima",
								  "notificarSolicitudes": true,
								  "notificarEstados": true,
								  "reporteSemanal": false,
								  "asignacionAutomatica": true,
								  "vencimientoReservas": false
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("\"nombreEmpresa\":\"Terraweb Inmobiliaria\"")));

		mockMvc.perform(get("/api/v1/admin/configuracion")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("\"correoContacto\":\"contacto@terraweb.pe\"")));

		mockMvc.perform(get("/admin/propiedades/nueva")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/propiedad-form"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("action=\"/admin/propiedades\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"imagenes\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("multiple")));

		mockMvc.perform(get("/admin/propiedades/{id}/editar", propiedad.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"imagenesConservar\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("Portada")));

		mockMvc.perform(get("/admin/agentes/nuevo")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/agente-form"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("action=\"/admin/agentes\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("name=\"foto\"")));
	}

	@Test
	void adminPuedeCrearEditarYRetirarPropiedad() throws Exception {
		mockMvc.perform(post("/admin/propiedades")
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("titulo", "Casa creada desde administración")
						.param("precio", "350000")
						.param("moneda", "PEN")
						.param("modalidad", "Venta")
						.param("tipo", "CASA")
						.param("estado", "DISPONIBLE")
						.param("publicada", "true"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/propiedades"));

		Propiedad creada = propiedadRepository.findAll().stream()
				.filter(propiedad -> "Casa creada desde administración".equals(propiedad.getTitulo()))
				.findFirst().orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals("CASA", creada.getTipo());
		mockMvc.perform(post("/admin/propiedades/{id}", creada.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("titulo", "Casa editada desde administración")
						.param("precio", "375000")
						.param("moneda", "PEN")
						.param("modalidad", "Venta")
						.param("tipo", "CASA")
						.param("estado", "DISPONIBLE")
						.param("publicada", "true"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/admin/propiedades/{id}/eliminar", creada.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());

		Propiedad retirada = propiedadRepository.findById(creada.getId()).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals("Casa editada desde administración", retirada.getTitulo());
		org.junit.jupiter.api.Assertions.assertFalse(retirada.isPublicada());
		org.junit.jupiter.api.Assertions.assertEquals("RETIRADA", retirada.getEstado());
	}

	@Test
	void editarPropiedadConservaFotosYAgregaLasNuevas() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Propiedad con galería conservada");
		propiedad.setPrecio(new BigDecimal("450000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		ImagenPropiedad portada = new ImagenPropiedad();
		portada.setUrl("/images/inmueble1.jpg");
		portada.setOrden(0);
		propiedad.agregarImagen(portada);
		propiedad = propiedadRepository.saveAndFlush(propiedad);
		Long propiedadId = propiedad.getId();

		MockMultipartFile imagenNueva = new MockMultipartFile(
				"imagenes",
				"sala.png",
				"image/png",
				new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0, 0, 0, 0});
		mockMvc.perform(multipart("/admin/propiedades/{id}", propiedadId)
						.file(imagenNueva)
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("titulo", "Propiedad con galería actualizada")
						.param("precio", "475000")
						.param("moneda", "PEN")
						.param("modalidad", "Venta")
						.param("tipo", "CASA")
						.param("estado", "DISPONIBLE")
						.param("publicada", "true")
						.param("imagenesConservar", portada.getId().toString()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/propiedades"));

		Propiedad guardada = propiedadRepository.findAllByOrderByFechaPublicacionDesc().stream()
				.filter(elemento -> elemento.getId().equals(propiedadId))
				.findFirst()
				.orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(2, guardada.getImagenes().size());
		org.junit.jupiter.api.Assertions.assertEquals("/images/inmueble1.jpg", guardada.getImagenes().get(0).getUrl());
		String imagenSubidaUrl = guardada.getImagenes().get(1).getUrl();
		org.junit.jupiter.api.Assertions.assertTrue(imagenSubidaUrl.startsWith("/uploads/"));

		propiedadRepository.deleteById(propiedadId);
		imagenStorageService.eliminar(List.of(imagenSubidaUrl));
	}

	@Test
	void adminPuedeCrearEditarYEliminarAgente() throws Exception {
		mockMvc.perform(post("/admin/agentes")
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("nombre", "Agente CRUD")
						.param("correo", "agente-crud@terraweb.local")
						.param("cargo", "Asesor de prueba")
						.param("especialidad", "Casas exclusivas y residencias")
						.param("estado", "DISPONIBLE")
						.param("telefono", "999 888 777")
						.param("activo", "true"))
				.andExpect(status().is3xxRedirection());

		Agente creado = agenteRepository.findAll().stream()
				.filter(agente -> "Agente CRUD".equals(agente.getNombre()))
				.findFirst().orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals("agente-crud@terraweb.local", creado.getCorreo());
		org.junit.jupiter.api.Assertions.assertEquals("Casas exclusivas y residencias", creado.getEspecialidad());
		mockMvc.perform(post("/admin/agentes/{id}", creado.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("nombre", "Agente CRUD editado")
						.param("correo", "agente-crud@terraweb.local")
						.param("cargo", "Asesor principal")
						.param("especialidad", "Casas exclusivas y residencias")
						.param("estado", "DISPONIBLE")
						.param("telefono", "999 888 777")
						.param("activo", "true"))
				.andExpect(status().is3xxRedirection());
		mockMvc.perform(post("/admin/agentes/{id}/eliminar", creado.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());

		org.junit.jupiter.api.Assertions.assertFalse(agenteRepository.existsById(creado.getId()));
	}

	@Test
	void adminPuedeArchivarYRestaurarUnaSolicitud() throws Exception {
		Propiedad propiedad = new Propiedad();
		propiedad.setTitulo("Propiedad para solicitud administrativa");
		propiedad.setPrecio(new BigDecimal("500000"));
		propiedad.setMoneda("PEN");
		propiedad.setModalidad("Venta");
		propiedad = propiedadRepository.saveAndFlush(propiedad);

		SolicitudVisita solicitud = new SolicitudVisita();
		solicitud.setNombre("Cliente solicitud");
		solicitud.setCorreo("solicitud@terraweb.local");
		solicitud.setTelefono("999999999");
		solicitud.setFechaEstimada(LocalDate.now().plusDays(3));
		solicitud.setPropiedad(propiedad);
		solicitud.setTituloPropiedad(propiedad.getTitulo());
		solicitud = solicitudVisitaRepository.saveAndFlush(solicitud);
		Long solicitudId = solicitud.getId();

		mockMvc.perform(put("/api/v1/admin/solicitudes/{id}/estado", solicitudId)
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.contentType(APPLICATION_JSON)
						.content("{\"estado\":\"NEGOCIACION\"}"))
				.andExpect(status().isOk());
		org.junit.jupiter.api.Assertions.assertEquals(
				"NEGOCIACION", solicitudVisitaRepository.findById(solicitudId).orElseThrow().getEstado());

		mockMvc.perform(post("/admin/solicitudes/{id}/archivar", solicitudId)
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());
		org.junit.jupiter.api.Assertions.assertTrue(
				solicitudVisitaRepository.findById(solicitudId).orElseThrow().isArchivada());
		org.junit.jupiter.api.Assertions.assertEquals(
				"CERRADA", solicitudVisitaRepository.findById(solicitudId).orElseThrow().getEstado());

		mockMvc.perform(post("/admin/solicitudes/{id}/restaurar", solicitudId)
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());
		org.junit.jupiter.api.Assertions.assertFalse(
				solicitudVisitaRepository.findById(solicitudId).orElseThrow().isArchivada());
		org.junit.jupiter.api.Assertions.assertEquals(
				"NUEVA", solicitudVisitaRepository.findById(solicitudId).orElseThrow().getEstado());
	}
}
