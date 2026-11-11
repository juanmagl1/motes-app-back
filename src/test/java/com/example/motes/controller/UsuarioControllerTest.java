package com.example.motes.controller;

import com.example.motes.domain.Usuario;
import com.example.motes.dto.UsuarioDTO;
import com.example.motes.security.JwtService;
import com.example.motes.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UsuarioController Tests")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private UsuarioDTO usuarioDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO(
                "user1", "test@test.com", "Test User",
                false, LocalDateTime.now()
        );

        usuario = new Usuario();
        usuario.setId("user1");
        usuario.setEmail("test@test.com");
        usuario.setNombre("Test User");
        usuario.setIsAdmin(false);
    }

    // ==================== GET /api/usuarios/creadores ====================

    @Test
    @DisplayName("obtenerCreadores - Con creadores - Retorna lista")
    void obtenerCreadores_ConCreadores_RetornaLista() throws Exception {
        // Given
        List<UsuarioDTO> creadores = Arrays.asList(usuarioDTO);
        when(usuarioService.obtenerCreadores()).thenReturn(creadores);

        // When & Then
        mockMvc.perform(get("/api/usuarios/creadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("test@test.com"));

        verify(usuarioService).obtenerCreadores();
    }

    @Test
    @DisplayName("obtenerCreadores - Sin creadores - Retorna lista vacía")
    void obtenerCreadores_SinCreadores_RetornaListaVacia() throws Exception {
        // Given
        when(usuarioService.obtenerCreadores()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/usuarios/creadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== GET /api/usuarios/{id} ====================

    @Test
    @DisplayName("obtenerPorId - Con ID existente - Retorna 200")
    void obtenerPorId_ConIdExistente_Retorna200() throws Exception {
        // Given
        when(usuarioService.buscarPorId("user1")).thenReturn(usuario);
        when(usuarioService.convertirADTO(usuario)).thenReturn(usuarioDTO);

        // When & Then
        mockMvc.perform(get("/api/usuarios/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user1"))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(usuarioService).buscarPorId("user1");
        verify(usuarioService).convertirADTO(usuario);
    }
}
