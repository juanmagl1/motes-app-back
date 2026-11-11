package com.example.motes.controller;

import com.example.motes.domain.Usuario;
import com.example.motes.dto.ErrorResponseDTO;
import com.example.motes.dto.LoginRequestDTO;
import com.example.motes.dto.LoginResponseDTO;
import com.example.motes.dto.RegisterRequestDTO;
import com.example.motes.dto.UsuarioDTO;
import com.example.motes.security.JwtService;
import com.example.motes.service.UsuarioService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioService usuarioService;

    // por si la config de seguridad lo necesita
    @MockBean
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioTest;
    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId("user1");
        usuarioTest.setEmail("nuevo@test.com");
        usuarioTest.setNombre("Test User");
        usuarioTest.setPassword("$2a$10$encodedPassword");
        usuarioTest.setIsAdmin(false);
        usuarioTest.setCreatedAt(LocalDateTime.now());

        usuarioDTO = new UsuarioDTO(
                usuarioTest.getId(),
                usuarioTest.getEmail(),
                usuarioTest.getNombre(),
                usuarioTest.getIsAdmin(),
                usuarioTest.getCreatedAt()
        );
    }

    // ========================================================================
    // /register
    // ========================================================================

    @Test
    @DisplayName("register - Con datos válidos - Retorna 200 y token")
    void register_ConDatosValidos_Retorna200YToken() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword("password123");
        request.setNombre("Nuevo Usuario");

        when(usuarioService.registrar("nuevo@test.com", "password123", "Nuevo Usuario"))
                .thenReturn(usuarioTest);
        when(jwtService.generateToken("nuevo@test.com")).thenReturn("fake.jwt.token");
        when(usuarioService.convertirADTO(usuarioTest)).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.usuario.email").value("nuevo@test.com"));

        verify(usuarioService).registrar("nuevo@test.com", "password123", "Nuevo Usuario");
    }

    @Test
    @DisplayName("register - Sin email - Retorna 400 y mensaje")
    void register_SinEmail_Retorna400YMensaje() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("   ");
        request.setPassword("password123");
        request.setNombre("Nombre");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El email es requerido"));

        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("register - Sin password - Retorna 400 y mensaje")
    void register_SinPassword_Retorna400YMensaje() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@test.com");
        request.setPassword("   ");
        request.setNombre("Nombre");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La contraseña es requerida"));

        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("register - Sin nombre - Retorna 400 y mensaje")
    void register_SinNombre_Retorna400YMensaje() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setNombre("   ");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El nombre es requerido"));

        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("register - Con email duplicado - Retorna 400 con mensaje de error")
    void register_ConEmailDuplicado_Retorna400() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("duplicado@test.com");
        request.setPassword("password123");
        request.setNombre("Usuario");

        when(usuarioService.registrar(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("El email ya está registrado"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));
    }

    @Test
    @DisplayName("register - Error inesperado - Retorna 400 con mensaje")
    void register_ErrorInesperado_Retorna400() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setNombre("Test");

        when(usuarioService.registrar(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de base de datos"));
    }

    // ========================================================================
    // /login
    // ========================================================================

    @Test
    @DisplayName("login - Con credenciales válidas - Retorna 200 y token")
    void login_ConCredencialesValidas_Retorna200YToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword("password123");

        when(usuarioService.buscarPorEmail("nuevo@test.com")).thenReturn(usuarioTest);
        when(usuarioService.validarPassword(usuarioTest, "password123")).thenReturn(true);
        when(jwtService.generateToken("nuevo@test.com")).thenReturn("fake.jwt.token");
        when(usuarioService.convertirADTO(usuarioTest)).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.usuario.email").value("nuevo@test.com"));
    }

    @Test
    @DisplayName("login - Sin email - Retorna 401")
    void login_SinEmail_Retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("   ");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).buscarPorEmail(anyString());
    }

    @Test
    @DisplayName("login - Sin password - Retorna 401")
    void login_SinPassword_Retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@test.com");
        request.setPassword("   ");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).buscarPorEmail(anyString());
    }

    @Test
    @DisplayName("login - Contraseña incorrecta - Retorna 401")
    void login_ConContrasenaIncorrecta_Retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword("wrongpassword");

        when(usuarioService.buscarPorEmail("nuevo@test.com")).thenReturn(usuarioTest);
        when(usuarioService.validarPassword(usuarioTest, "wrongpassword")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login - Usuario inexistente - Retorna 401")
    void login_ConUsuarioInexistente_Retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("noexiste@test.com");
        request.setPassword("password123");

        when(usuarioService.buscarPorEmail("noexiste@test.com"))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
