package com.example.motes.controller;

import com.example.motes.domain.Usuario;
import com.example.motes.dto.MoteRequestDTO;
import com.example.motes.dto.MoteResponseDTO;
import com.example.motes.security.JwtService;
import com.example.motes.service.MoteService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MoteController.class)
@AutoConfigureMockMvc
@WithMockUser(username = "test@test.com", roles = {"USER", "ADMIN"})
@DisplayName("MoteController Tests")
class MoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MoteService moteService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private Usuario usuarioTest;
    private MoteResponseDTO moteResponse;
    private MoteRequestDTO moteRequest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId("user1");
        usuarioTest.setEmail("test@test.com");
        usuarioTest.setNombre("Test User");
        // para que getIsAdmin() no sea null y coincida con lo que mockeamos
        usuarioTest.setIsAdmin(true);

        moteResponse = new MoteResponseDTO(
                "mote1",
                "persona1",
                "Juan",
                "El Rayo",
                "Descripcion",
                null,
                5,
                "user1",
                LocalDateTime.now()
        );

        moteRequest = new MoteRequestDTO();
        moteRequest.setPersonaId("persona1");
        moteRequest.setMote("El Rayo");
        moteRequest.setDescripcion("Descripcion");
    }

    // ==================== POST /api/motes ====================

    @Test
    @DisplayName("crear - Con datos válidos (imagen válida) - Retorna 200")
    void crear_ConDatosValidos_Retorna200() throws Exception {
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(moteService.crear(any(MoteRequestDTO.class), eq("user1"))).thenReturn(moteResponse);

        // imagen con formato permitido para cubrir isValidImageFormat = true
        moteRequest.setImagen("data:image/png;base64,AAAAAA");

        mockMvc.perform(post("/api/motes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("mote1"))
                .andExpect(jsonPath("$.mote").value("El Rayo"));

        verify(moteService).crear(any(MoteRequestDTO.class), eq("user1"));
    }

    @Test
    @DisplayName("crear - Sin personaId - Retorna 400 y mensaje")
    void crear_SinPersona_Retorna400YMensaje() throws Exception {
        moteRequest.setPersonaId("   ");

        mockMvc.perform(post("/api/motes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La persona es requerida"));

        verify(moteService, never()).crear(any(MoteRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Sin mote - Retorna 400 y mensaje")
    void crear_SinMote_Retorna400YMensaje() throws Exception {
        moteRequest.setMote("   ");

        mockMvc.perform(post("/api/motes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El mote es requerido"));

        verify(moteService, never()).crear(any(MoteRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Sin descripción - Retorna 400 y mensaje")
    void crear_SinDescripcion_Retorna400YMensaje() throws Exception {
        moteRequest.setDescripcion("   ");

        mockMvc.perform(post("/api/motes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La descripción es requerida"));

        verify(moteService, never()).crear(any(MoteRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Imagen con formato inválido - Retorna 400 y mensaje")
    void crear_ImagenFormatoInvalido_Retorna400YMensaje() throws Exception {
        moteRequest.setImagen("data:image/tiff;base64,AAAAAA");

        mockMvc.perform(post("/api/motes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Solo se permiten imágenes con formato: JPG, JPEG, PNG, GIF, SVG, WEBP, BMP, AVIF"));

        verify(moteService, never()).crear(any(MoteRequestDTO.class), anyString());
    }

    @Test
    @DisplayName("crear - Error inesperado en servicio - Retorna 400")
    void crear_ErrorInesperado_Retorna400() throws Exception {
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(moteService.crear(any(MoteRequestDTO.class), eq("user1")))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/api/motes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de base de datos"));
    }

    // ==================== GET /api/motes ====================

    @Test
    @DisplayName("obtenerTodos - Sin filtros - Retorna lista completa")
    void obtenerTodos_SinFiltros_RetornaListaCompleta() throws Exception {
        List<MoteResponseDTO> motes = Arrays.asList(moteResponse);
        when(moteService.obtenerTodos()).thenReturn(motes);

        mockMvc.perform(get("/api/motes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].mote").value("El Rayo"));

        verify(moteService).obtenerTodos();
        verify(moteService, never()).filtrar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("obtenerTodos - Con filtro nombreMote - Llama a filtrar")
    void obtenerTodos_ConFiltroNombreMote_LlamaAFiltrar() throws Exception {
        when(moteService.filtrar("Rayo", null, null)).thenReturn(Arrays.asList(moteResponse));

        mockMvc.perform(get("/api/motes")
                        .param("nombreMote", "Rayo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(moteService).filtrar("Rayo", null, null);
        verify(moteService, never()).obtenerTodos();
    }

    @Test
    @DisplayName("obtenerTodos - Con filtro personaId - Llama a filtrar")
    void obtenerTodos_ConFiltroPersonaId_LlamaAFiltrar() throws Exception {
        when(moteService.filtrar(null, "persona1", null)).thenReturn(Arrays.asList(moteResponse));

        mockMvc.perform(get("/api/motes")
                        .param("personaId", "persona1"))
                .andExpect(status().isOk());

        verify(moteService).filtrar(null, "persona1", null);
    }

    @Test
    @DisplayName("obtenerTodos - Con filtro creadorId - Llama a filtrar")
    void obtenerTodos_ConFiltroCreadorId_LlamaAFiltrar() throws Exception {
        when(moteService.filtrar(null, null, "user1")).thenReturn(Arrays.asList(moteResponse));

        mockMvc.perform(get("/api/motes")
                        .param("creadorId", "user1"))
                .andExpect(status().isOk());

        verify(moteService).filtrar(null, null, "user1");
    }

    // ==================== GET /api/motes/top5 ====================

    @Test
    @DisplayName("obtenerTop5 - Retorna top 5 motes")
    void obtenerTop5_RetornaTop5Motes() throws Exception {
        when(moteService.obtenerTop5()).thenReturn(Arrays.asList(moteResponse));

        mockMvc.perform(get("/api/motes/top5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(moteService).obtenerTop5();
    }

    // ==================== GET /api/motes/destacado/{personaId} ====================

    @Test
    @DisplayName("obtenerMoteDestacado - Con mote destacado - Retorna 200")
    void obtenerMoteDestacado_ConMoteDestacado_Retorna200() throws Exception {
        when(moteService.obtenerMoteDestacado("persona1")).thenReturn(moteResponse);

        mockMvc.perform(get("/api/motes/destacado/persona1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mote").value("El Rayo"));
    }

    @Test
    @DisplayName("obtenerMoteDestacado - Sin mote destacado - Retorna 404")
    void obtenerMoteDestacado_SinMoteDestacado_Retorna404() throws Exception {
        when(moteService.obtenerMoteDestacado("persona999")).thenReturn(null);

        mockMvc.perform(get("/api/motes/destacado/persona999"))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /api/motes/{id} ====================

    @Test
    @DisplayName("obtenerPorId - Con ID existente - Retorna 200")
    void obtenerPorId_ConIdExistente_Retorna200() throws Exception {
        when(moteService.obtenerPorId("mote1")).thenReturn(moteResponse);

        mockMvc.perform(get("/api/motes/mote1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("mote1"));
    }

    // ==================== PUT /api/motes/{id} ====================

    @Test
    @DisplayName("actualizar - Con datos válidos (imagen válida) - Retorna 200")
    void actualizar_ConDatosValidos_Retorna200() throws Exception {
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        moteRequest.setImagen("data:image/jpeg;base64,BBBBBB");

        when(moteService.actualizar(eq("mote1"), any(MoteRequestDTO.class), eq("user1"), eq(true)))
                .thenReturn(moteResponse);

        mockMvc.perform(put("/api/motes/mote1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mote").value("El Rayo"));

        verify(moteService).actualizar(eq("mote1"), any(MoteRequestDTO.class), eq("user1"), eq(true));
    }

    @Test
    @DisplayName("actualizar - Sin personaId - Retorna 400 y mensaje")
    void actualizar_SinPersona_Retorna400YMensaje() throws Exception {
        moteRequest.setPersonaId("   ");

        mockMvc.perform(put("/api/motes/mote1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La persona es requerida"));

        verify(moteService, never()).actualizar(anyString(), any(MoteRequestDTO.class), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("actualizar - Sin mote - Retorna 400 y mensaje")
    void actualizar_SinMote_Retorna400YMensaje() throws Exception {
        moteRequest.setMote("   ");

        mockMvc.perform(put("/api/motes/mote1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El mote es requerido"));

        verify(moteService, never()).actualizar(anyString(), any(MoteRequestDTO.class), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("actualizar - Sin descripción - Retorna 400 y mensaje")
    void actualizar_SinDescripcion_Retorna400YMensaje() throws Exception {
        moteRequest.setDescripcion("   ");

        mockMvc.perform(put("/api/motes/mote1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La descripción es requerida"));

        verify(moteService, never()).actualizar(anyString(), any(MoteRequestDTO.class), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("actualizar - Imagen con formato inválido - Retorna 400 y mensaje")
    void actualizar_ImagenFormatoInvalido_Retorna400YMensaje() throws Exception {
        moteRequest.setImagen("data:image/tiff;base64,CCCCCC");

        mockMvc.perform(put("/api/motes/mote1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Solo se permiten imágenes con formato: JPG, JPEG, PNG, GIF, SVG, WEBP, BMP, AVIF"));

        verify(moteService, never()).actualizar(anyString(), any(MoteRequestDTO.class), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("actualizar - Error inesperado en servicio - Retorna 400")
    void actualizar_ErrorInesperado_Retorna400() throws Exception {
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        when(moteService.actualizar(eq("mote1"), any(MoteRequestDTO.class), eq("user1"), eq(true)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(put("/api/motes/mote1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de base de datos"));
    }

    // ==================== DELETE /api/motes/{id} ====================

    @Test
    @DisplayName("eliminar - Con ID válido - Retorna 204")
    void eliminar_ConIdValido_Retorna204() throws Exception {
        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(usuarioTest);
        doNothing().when(moteService).eliminar("mote1", "user1", true);

        mockMvc.perform(delete("/api/motes/mote1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(moteService).eliminar("mote1", "user1", true);
    }
}
