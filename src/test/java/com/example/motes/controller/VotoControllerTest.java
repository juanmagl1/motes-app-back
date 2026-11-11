package com.example.motes.controller;

import com.example.motes.domain.Usuario;
import com.example.motes.dto.VotoRequestDTO;
import com.example.motes.dto.VotoResponseDTO;
import com.example.motes.security.JwtService;
import com.example.motes.service.UsuarioService;
import com.example.motes.service.VotoService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
@AutoConfigureMockMvc
@WithMockUser(username = "test@test.com")  // todas las peticiones van autenticadas con este usuario
@DisplayName("VotoController Tests")
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VotoService votoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private Usuario usuarioTest;
    private VotoRequestDTO votoRequest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId("user1");
        usuarioTest.setEmail("test@test.com");
        usuarioTest.setNombre("Test User");

        votoRequest = new VotoRequestDTO();
        votoRequest.setMoteId("mote1");
    }

    // ==================== POST /api/votos/toggle ====================

    @Test
    @DisplayName("toggleVoto - Usuario vota por primera vez - Retorna voted=true")
    void toggleVoto_UsuarioVotaPorPrimeraVez_RetornaVotedTrue() throws Exception {
        // Given
        VotoResponseDTO response = new VotoResponseDTO(true, 1);
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(votoService.toggleVoto("user1", "mote1")).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/votos/toggle")
                        .with(csrf()) // <- necesario para que no devuelva 403 por CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(votoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voted").value(true))
                .andExpect(jsonPath("$.votos").value(1));

        verify(votoService).toggleVoto("user1", "mote1");
    }

    @Test
    @DisplayName("toggleVoto - Usuario quita su voto - Retorna voted=false")
    void toggleVoto_UsuarioQuitaSuVoto_RetornaVotedFalse() throws Exception {
        // Given
        VotoResponseDTO response = new VotoResponseDTO(false, 0);
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(votoService.toggleVoto("user1", "mote1")).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/votos/toggle")
                        .with(csrf()) // <- idem aquí
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(votoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voted").value(false))
                .andExpect(jsonPath("$.votos").value(0));

        verify(votoService).toggleVoto("user1", "mote1");
    }

    // ==================== GET /api/votos/check/{moteId} ====================

    @Test
    @DisplayName("hasVotado - Usuario ha votado - Retorna true")
    void hasVotado_UsuarioHaVotado_RetornaTrue() throws Exception {
        // Given
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(votoService.hasVotado("user1", "mote1")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/votos/check/mote1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(votoService).hasVotado("user1", "mote1");
    }

    @Test
    @DisplayName("hasVotado - Usuario no ha votado - Retorna false")
    void hasVotado_UsuarioNoHaVotado_RetornaFalse() throws Exception {
        // Given
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(votoService.hasVotado("user1", "mote1")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/votos/check/mote1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
