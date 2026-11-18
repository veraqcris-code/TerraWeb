package com.inmobiliaria.proyecto;

import com.inmobiliaria.proyecto.controller.InmuebleController;
import com.inmobiliaria.proyecto.service.InmuebleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InmuebleControllerTest {

    private MockMvc mockMvc;
    private InmuebleService service;

    @BeforeEach
    public void setUp() {
        service = mock(InmuebleService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new InmuebleController(service)).build();
    }

    @Test
    public void debeRetornarCodigo200AlPedirListaDeInmuebles() throws Exception {
        when(service.obtenerTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/inmuebles"))
                .andExpect(status().isOk());
    }
}