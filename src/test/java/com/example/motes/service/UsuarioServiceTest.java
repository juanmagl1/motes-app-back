package com.example.motes.service;

import com.example.motes.domain.Persona;
import com.example.motes.domain.Usuario;
import com.example.motes.dto.UsuarioDTO;
import com.example.motes.repository.PersonaRepository;
import com.example.motes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService Tests")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId("user1");
        // dominio válido según validarEmail()
        usuarioTest.setEmail("test@gmail.com");
        usuarioTest.setNombre("Test User");
        usuarioTest.setPassword("$2a$10$encodedPassword");
        usuarioTest.setIsAdmin(false);
        usuarioTest.setCreatedAt(LocalDateTime.now());
    }

    // ==================== Tests de registrar() ====================

    @Test
    @DisplayName("registrar - Con datos válidos - Retorna usuario y crea persona")
    void registrar_ConDatosValidos_RetornaUsuarioYCreaPersona() {
        // Given
        String email = "nuevo@gmail.com";

        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioTest);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());

        // When
        Usuario resultado = usuarioService.registrar(email, "password123", "Nuevo Usuario");

        // Then
        assertNotNull(resultado);
        verify(usuarioRepository).existsByEmail(email);
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(personaRepository).save(any(Persona.class));
    }

    @Test
    @DisplayName("registrar - Email con formato inválido - Lanza excepción")
    void registrar_EmailConFormatoInvalido_LanzaExcepcion() {
        // Given
        String email = "test@dominioNoValido.com"; // dominio NO permitido

        // No stubbear existsByEmail: no debería llegar a llamarse

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrar(email, "password123", "Test");
        });

        assertEquals("El formato de email no es válido", exception.getMessage());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(personaRepository, never()).save(any(Persona.class));
    }

    @Test
    @DisplayName("registrar - Con email duplicado - Lanza excepción")
    void registrar_ConEmailDuplicado_LanzaExcepcion() {
        // Given
        String email = "test@gmail.com";

        when(usuarioRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrar(email, "password123", "Test");
        });

        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioRepository).existsByEmail(email);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(personaRepository, never()).save(any(Persona.class));
    }

    @Test
    @DisplayName("registrar - Persona creada tiene userId correcto")
    void registrar_PersonaCreadaTieneUserIdCorrecto() {
        // Given
        String email = "test@gmail.com";

        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioTest);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());

        // When
        usuarioService.registrar(email, "pass", "Test");

        // Then
        verify(usuarioRepository).existsByEmail(email);
        verify(personaRepository).save(argThat(persona ->
                persona.getUserId() != null &&
                        persona.getCreatedBy() != null &&
                        persona.getUserId().equals(persona.getCreatedBy())
        ));
    }

    @Test
    @DisplayName("registrar - Usuario no es admin por defecto")
    void registrar_UsuarioNoEsAdminPorDefecto() {
        // Given
        String email = "test@gmail.com";

        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioTest);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());

        // When
        usuarioService.registrar(email, "pass", "Test");

        // Then
        verify(usuarioRepository).existsByEmail(email);
        verify(usuarioRepository).save(argThat(usuario -> !usuario.getIsAdmin()));
    }

    // ==================== Tests de buscarPorEmail() ====================

    @Test
    @DisplayName("buscarPorEmail - Con email existente - Retorna usuario")
    void buscarPorEmail_ConEmailExistente_RetornaUsuario() {
        // Given
        when(usuarioRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(usuarioTest));

        // When
        Usuario resultado = usuarioService.buscarPorEmail("test@gmail.com");

        // Then
        assertNotNull(resultado);
        assertEquals("test@gmail.com", resultado.getEmail());
    }

    @Test
    @DisplayName("buscarPorEmail - Con email inexistente - Lanza excepción")
    void buscarPorEmail_ConEmailInexistente_LanzaExcepcion() {
        // Given
        when(usuarioRepository.findByEmail("noexiste@gmail.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.buscarPorEmail("noexiste@gmail.com");
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ==================== Tests de buscarPorId() ====================

    @Test
    @DisplayName("buscarPorId - Con ID existente - Retorna usuario")
    void buscarPorId_ConIdExistente_RetornaUsuario() {
        // Given
        when(usuarioRepository.findById("user1")).thenReturn(Optional.of(usuarioTest));

        // When
        Usuario resultado = usuarioService.buscarPorId("user1");

        // Then
        assertNotNull(resultado);
        assertEquals("user1", resultado.getId());
    }

    @Test
    @DisplayName("buscarPorId - Con ID inexistente - Lanza excepción")
    void buscarPorId_ConIdInexistente_LanzaExcepcion() {
        // Given
        when(usuarioRepository.findById("user999")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.buscarPorId("user999");
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ==================== Tests de validarPassword() ====================

    @Test
    @DisplayName("validarPassword - Con contraseña correcta - Retorna true")
    void validarPassword_ConContrasenaCorrecta_RetornaTrue() {
        // Given
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        // When
        boolean resultado = usuarioService.validarPassword(usuarioTest, "password123");

        // Then
        assertTrue(resultado);
    }

    @Test
    @DisplayName("validarPassword - Con contraseña incorrecta - Retorna false")
    void validarPassword_ConContrasenaIncorrecta_RetornaFalse() {
        // Given
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);

        // When
        boolean resultado = usuarioService.validarPassword(usuarioTest, "wrongpassword");

        // Then
        assertFalse(resultado);
    }

    // ==================== Tests de obtenerCreadores() ====================

    @Test
    @DisplayName("obtenerCreadores - Con creadores - Retorna lista de DTOs")
    void obtenerCreadores_ConCreadores_RetornaListaDeDTOs() {
        // Given
        Usuario user2 = new Usuario();
        user2.setId("user2");
        user2.setEmail("creator@gmail.com");
        user2.setNombre("Creator");
        user2.setIsAdmin(false);

        when(usuarioRepository.findUsuariosQueHanCreadoMotes())
                .thenReturn(Arrays.asList(usuarioTest, user2));

        // When
        List<UsuarioDTO> resultado = usuarioService.obtenerCreadores();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("test@gmail.com", resultado.get(0).getEmail());
        assertEquals("creator@gmail.com", resultado.get(1).getEmail());
    }

    @Test
    @DisplayName("obtenerCreadores - Sin creadores - Retorna lista vacía")
    void obtenerCreadores_SinCreadores_RetornaListaVacia() {
        // Given
        when(usuarioRepository.findUsuariosQueHanCreadoMotes())
                .thenReturn(Collections.emptyList());

        // When
        List<UsuarioDTO> resultado = usuarioService.obtenerCreadores();

        // Then
        assertTrue(resultado.isEmpty());
    }

    // ==================== Tests de convertirADTO() ====================

    @Test
    @DisplayName("convertirADTO - Convierte correctamente a DTO")
    void convertirADTO_ConvierteCorrectamenteADTO() {
        // When
        UsuarioDTO resultado = usuarioService.convertirADTO(usuarioTest);

        // Then
        assertNotNull(resultado);
        assertEquals("user1", resultado.getId());
        assertEquals("test@gmail.com", resultado.getEmail());
        assertEquals("Test User", resultado.getNombre());
        assertFalse(resultado.getIsAdmin());
        assertNotNull(resultado.getCreatedAt());
    }
}
