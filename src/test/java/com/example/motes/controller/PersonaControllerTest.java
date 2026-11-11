package com.example.motes.controller;

import com.example.motes.dto.PersonaRequestDTO;
import com.example.motes.dto.PersonaResponseDTO;
import com.example.motes.security.JwtService;
import com.example.motes.service.PersonaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonaController.class)
@AutoConfigureMockMvc
@WithMockUser(username = "test@test.com") // usuario autenticado para TODOS los tests
@DisplayName("PersonaController Tests")
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonaService personaService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private PersonaResponseDTO personaResponse;
    private PersonaRequestDTO personaRequest;

    @BeforeEach
    void setUp() {
        personaResponse = new PersonaResponseDTO(
                "persona1", "Juan Perez", "juan@test.com",
                null, "user1", LocalDateTime.now()
        );

        personaRequest = new PersonaRequestDTO();
        personaRequest.setNombre("Juan Perez");
        personaRequest.setEmail("juan@test.com");
        personaRequest.setCrearCuenta(false);
    }

    // ==================== POST /api/personas ====================

    @Test
    @DisplayName("crear - Con datos válidos - Retorna 200")
    void crear_ConDatosValidos_Retorna200() throws Exception {
        // Given
        when(personaService.crear(any(PersonaRequestDTO.class), eq("test@test.com")))
                .thenReturn(personaResponse);

        // When & Then
        mockMvc.perform(post("/api/personas")
                        .with(csrf()) // necesario para evitar 403 por CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("persona1"))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"));

        verify(personaService).crear(any(PersonaRequestDTO.class), eq("test@test.com"));
    }

    @Test
    @DisplayName("crear - Sin nombre - Retorna 400 y mensaje de error")
    void crear_SinNombre_Retorna400YMensaje() throws Exception {
        // Given
        personaRequest.setNombre("   "); // vacío/espacios

        // When & Then
        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El nombre es requerido"));

        // No debe llamarse al servicio
        verify(personaService, never()).crear(any(PersonaRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Crear cuenta sin email - Retorna 400 y mensaje")
    void crear_CrearCuentaSinEmail_Retorna400YMensaje() throws Exception {
        // Given
        personaRequest.setCrearCuenta(true);
        personaRequest.setEmail("   ");   // vacío
        personaRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("El email es requerido para crear una cuenta"));

        verify(personaService, never()).crear(any(PersonaRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Crear cuenta sin password - Retorna 400 y mensaje")
    void crear_CrearCuentaSinPassword_Retorna400YMensaje() throws Exception {
        // Given
        personaRequest.setCrearCuenta(true);
        personaRequest.setEmail("juan@test.com");
        personaRequest.setPassword("   ");  // vacío

        // When & Then
        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("La contraseña es requerida para crear una cuenta"));

        verify(personaService, never()).crear(any(PersonaRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Con email duplicado - Retorna 400")
    void crear_ConEmailDuplicado_Retorna400() throws Exception {
        // Given
        personaRequest.setCrearCuenta(true);
        personaRequest.setPassword("password123");
        when(personaService.crear(any(PersonaRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("El email ya está registrado"));

        // When & Then
        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));
    }

    @Test
    @DisplayName("crear - Error inesperado - Retorna 400")
    void crear_ErrorInesperado_Retorna400() throws Exception {
        // Given
        when(personaService.crear(any(PersonaRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // When & Then
        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/personas ====================

    @Test
    @DisplayName("obtenerTodas - Con personas - Retorna lista")
    void obtenerTodas_ConPersonas_RetornaLista() throws Exception {
        // Given
        List<PersonaResponseDTO> personas = Arrays.asList(personaResponse);
        when(personaService.obtenerTodas()).thenReturn(personas);

        // When & Then
        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"));

        verify(personaService).obtenerTodas();
    }

    @Test
    @DisplayName("obtenerTodas - Sin personas - Retorna lista vacía")
    void obtenerTodas_SinPersonas_RetornaListaVacia() throws Exception {
        // Given
        when(personaService.obtenerTodas()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== GET /api/personas/{id} ====================

    @Test
    @DisplayName("obtenerPorId - Con ID existente - Retorna 200")
    void obtenerPorId_ConIdExistente_Retorna200() throws Exception {
        // Given
        when(personaService.obtenerPorId("persona1")).thenReturn(personaResponse);

        // When & Then
        mockMvc.perform(get("/api/personas/persona1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("persona1"))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"));
    }
}
