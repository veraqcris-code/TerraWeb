package com.inmobiliaria.proyecto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import com.inmobiliaria.proyecto.models.ImagenPropiedad;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.RolUsuario;
import com.inmobiliaria.proyecto.models.SolicitudVisita;
import com.inmobiliaria.proyecto.models.Usuario;
import com.inmobiliaria.proyecto.repository.AgenteRepository;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.SolicitudVisitaRepository;
import com.inmobiliaria.proyecto.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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

	@Test
	void contextLoads() {
	}

	@Test
	void inicioRenderizaLaPlantillaThymeleaf() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("inicio"))
				.andExpect(MockMvcResultMatchers.content().string(
						org.hamcrest.Matchers.containsString("Últimas")));
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

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", "admin-test@terraweb.local")
						.param("password", "admin123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "/admin"));
	}

	@Test
	void registroPublicoSiempreCreaUnClienteAunqueSeEnvieUnRol() throws Exception {
		mockMvc.perform(post("/registro")
						.with(csrf())
						.param("nombre", "Cliente de prueba")
						.param("correo", "cliente-registro@terraweb.local")
						.param("password", "ClienteSeguro123!")
						.param("confirmarPassword", "ClienteSeguro123!")
						.param("rol", "ADMIN"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));

		Usuario usuario = usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue("cliente-registro@terraweb.local")
				.orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(RolUsuario.CLIENTE, usuario.getRol());
		org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches(
				"ClienteSeguro123!", usuario.getPasswordHash()));
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
		ImagenPropiedad imagen = new ImagenPropiedad();
		imagen.setUrl("/images/inmueble1.jpg");
		imagen.setOrden(0);
		propiedad.agregarImagen(imagen);
		propiedadRepository.saveAndFlush(propiedad);

		mockMvc.perform(get("/admin")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("Propiedad visible en el panel")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("aria-label=\"Navegación administrativa\"")))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("href=\"#propiedades\"")));

		mockMvc.perform(get("/admin/propiedades/nueva")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/propiedad-form"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("action=\"/admin/propiedades\"")));

		mockMvc.perform(get("/admin/agentes/nuevo")
						.with(user("admin-test@terraweb.local").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/agente-form"))
				.andExpect(content().string(
						org.hamcrest.Matchers.containsString("action=\"/admin/agentes\"")));
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
						.param("estado", "DISPONIBLE")
						.param("publicada", "true"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin"));

		Propiedad creada = propiedadRepository.findAll().stream()
				.filter(propiedad -> "Casa creada desde administración".equals(propiedad.getTitulo()))
				.findFirst().orElseThrow();
		mockMvc.perform(post("/admin/propiedades/{id}", creada.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("titulo", "Casa editada desde administración")
						.param("precio", "375000")
						.param("moneda", "PEN")
						.param("modalidad", "Venta")
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
	void adminPuedeCrearEditarYEliminarAgente() throws Exception {
		mockMvc.perform(post("/admin/agentes")
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("nombre", "Agente CRUD")
						.param("cargo", "Asesor de prueba")
						.param("activo", "true"))
				.andExpect(status().is3xxRedirection());

		Agente creado = agenteRepository.findAll().stream()
				.filter(agente -> "Agente CRUD".equals(agente.getNombre()))
				.findFirst().orElseThrow();
		mockMvc.perform(post("/admin/agentes/{id}", creado.getId())
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf())
						.param("nombre", "Agente CRUD editado")
						.param("cargo", "Asesor principal")
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

		mockMvc.perform(post("/admin/solicitudes/{id}/archivar", solicitudId)
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());
		org.junit.jupiter.api.Assertions.assertTrue(
				solicitudVisitaRepository.findById(solicitudId).orElseThrow().isArchivada());

		mockMvc.perform(post("/admin/solicitudes/{id}/restaurar", solicitudId)
						.with(user("admin-test@terraweb.local").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());
		org.junit.jupiter.api.Assertions.assertFalse(
				solicitudVisitaRepository.findById(solicitudId).orElseThrow().isArchivada());
	}
}
