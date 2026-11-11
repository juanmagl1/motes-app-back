package com.example.motes.service;

import com.example.motes.domain.Persona;
import com.example.motes.domain.Usuario;
import com.example.motes.dto.PersonaRequestDTO;
import com.example.motes.dto.PersonaResponseDTO;
import com.example.motes.repository.PersonaRepository;
import com.example.motes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("PersonaService Tests")
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PersonaService personaService;

    private Persona personaTest;
    private PersonaRequestDTO requestDTO;
    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        personaTest = new Persona();
        personaTest.setId("persona1");
        personaTest.setNombre("Juan Perez");
        personaTest.setEmail("juan@gmail.com");  // dominio válido
        personaTest.setCreatedBy("user1");
        personaTest.setCreatedAt(LocalDateTime.now());

        requestDTO = new PersonaRequestDTO();
        requestDTO.setNombre("Juan Perez");
        requestDTO.setEmail("juan@gmail.com");

        usuarioTest = new Usuario();
        usuarioTest.setId("user123");
        usuarioTest.setEmail("juan@gmail.com");
        usuarioTest.setNombre("Juan Perez");
    }

    // ==================== Tests de crear() ====================

    @Test
    @DisplayName("crear - Nombre duplicado - Lanza excepción")
    void crear_NombreDuplicado_LanzaExcepcion() {
        // Given
        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(List.of(personaTest));   // lista NO vacía

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                personaService.crear(requestDTO, "user1")
        );

        assertEquals("Ya existe una persona con ese nombre", ex.getMessage());
        verify(personaRepository, never()).save(any(Persona.class));
        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("crear - Sin crear cuenta - Retorna persona creada")
    void crear_SinCrearCuenta_RetornaPersonaCreada() {
        // Given
        requestDTO.setCrearCuenta(false);
        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(Collections.emptyList());
        when(personaRepository.save(any(Persona.class))).thenReturn(personaTest);

        // When
        PersonaResponseDTO resultado = personaService.crear(requestDTO, "user1");

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getNombre());
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("crear - Con crear cuenta true - Crea usuario y persona")
    void crear_ConCrearCuentaTrue_CreaUsuarioYPersona() {
        // Given
        requestDTO.setCrearCuenta(true);
        requestDTO.setPassword("password123");

        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(Collections.emptyList());
        when(usuarioRepository.existsByEmail("juan@gmail.com")).thenReturn(false);
        when(usuarioService.registrar("juan@gmail.com", "password123", "Juan Perez"))
                .thenReturn(usuarioTest);
        when(personaRepository.save(any(Persona.class))).thenReturn(personaTest);

        // When
        PersonaResponseDTO resultado = personaService.crear(requestDTO, "user1");

        // Then
        assertNotNull(resultado);
        verify(usuarioRepository).existsByEmail("juan@gmail.com");
        verify(usuarioService).registrar("juan@gmail.com", "password123", "Juan Perez");
        verify(personaRepository).save(argThat(p -> p.getUserId() != null));
    }

    @Test
    @DisplayName("crear - Con crear cuenta pero sin email - No crea usuario")
    void crear_ConCrearCuentaPeroSinEmail_NoCreUsuario() {
        // Given
        requestDTO.setCrearCuenta(true);
        requestDTO.setEmail(null);
        requestDTO.setPassword("password123");

        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(Collections.emptyList());
        when(personaRepository.save(any(Persona.class))).thenReturn(personaTest);

        // When
        PersonaResponseDTO resultado = personaService.crear(requestDTO, "user1");

        // Then
        assertNotNull(resultado);
        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("crear - Con crear cuenta pero sin password - No crea usuario")
    void crear_ConCrearCuentaPeroSinPassword_NoCreUsuario() {
        // Given
        requestDTO.setCrearCuenta(true);
        requestDTO.setPassword(null);

        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(Collections.emptyList());
        when(personaRepository.save(any(Persona.class))).thenReturn(personaTest);

        // When
        PersonaResponseDTO resultado = personaService.crear(requestDTO, "user1");

        // Then
        assertNotNull(resultado);
        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("crear - Email duplicado al crear cuenta - Lanza excepción")
    void crear_EmailDuplicadoAlCrearCuenta_LanzaExcepcion() {
        // Given
        requestDTO.setCrearCuenta(true);
        requestDTO.setPassword("password123");

        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(Collections.emptyList());
        when(usuarioRepository.existsByEmail("juan@gmail.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            personaService.crear(requestDTO, "user1");
        });

        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioRepository).existsByEmail("juan@gmail.com");
        verify(personaRepository, never()).save(any(Persona.class));
        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("crear - Email con formato inválido - Lanza excepción")
    void crear_EmailConFormatoInvalido_LanzaExcepcion() {
        // Given
        requestDTO.setCrearCuenta(true);
        requestDTO.setPassword("password123");
        requestDTO.setEmail("juan@dominioNoValido.com"); // dominio NO permitido

        when(personaRepository.findByNombre("Juan Perez"))
                .thenReturn(Collections.emptyList());
        // No stubbear usuarioRepository.existsByEmail -> no debería llamarse

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            personaService.crear(requestDTO, "user1");
        });

        assertEquals("El formato de email no es válido", exception.getMessage());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(personaRepository, never()).save(any(Persona.class));
        verify(usuarioService, never()).registrar(anyString(), anyString(), anyString());
    }

    // ==================== Tests de obtenerTodas() ====================

    @Test
    @DisplayName("obtenerTodas - Con personas existentes - Retorna lista")
    void obtenerTodas_ConPersonasExistentes_RetornaLista() {
        // Given
        Persona persona2 = new Persona();
        persona2.setId("persona2");
        persona2.setNombre("Maria Lopez");
        persona2.setEmail("maria@gmail.com");
        persona2.setCreatedBy("user1");

        when(personaRepository.findAll()).thenReturn(Arrays.asList(personaTest, persona2));

        // When
        List<PersonaResponseDTO> resultado = personaService.obtenerTodas();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
        assertEquals("Maria Lopez", resultado.get(1).getNombre());
    }

    @Test
    @DisplayName("obtenerTodas - Sin personas - Retorna lista vacía")
    void obtenerTodas_SinPersonas_RetornaListaVacia() {
        // Given
        when(personaRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<PersonaResponseDTO> resultado = personaService.obtenerTodas();

        // Then
        assertTrue(resultado.isEmpty());
    }

    // ==================== Tests de obtenerPorId() ====================

    @Test
    @DisplayName("obtenerPorId - Con ID existente - Retorna persona")
    void obtenerPorId_ConIdExistente_RetornaPersona() {
        // Given
        when(personaRepository.findById("persona1")).thenReturn(Optional.of(personaTest));

        // When
        PersonaResponseDTO resultado = personaService.obtenerPorId("persona1");

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals("persona1", resultado.getId());
    }

    @Test
    @DisplayName("obtenerPorId - Con ID inexistente - Lanza excepción")
    void obtenerPorId_ConIdInexistente_LanzaExcepcion() {
        // Given
        when(personaRepository.findById("persona999")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            personaService.obtenerPorId("persona999");
        });

        assertEquals("Persona no encontrada", exception.getMessage());
    }

    // ==================== Tests de obtenerEntidadPorId() ====================

    @Test
    @DisplayName("obtenerEntidadPorId - Con ID existente - Retorna entidad")
    void obtenerEntidadPorId_ConIdExistente_RetornaEntidad() {
        // Given
        when(personaRepository.findById("persona1")).thenReturn(Optional.of(personaTest));

        // When
        Persona resultado = personaService.obtenerEntidadPorId("persona1");

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getNombre());
        assertInstanceOf(Persona.class, resultado);
    }

    @Test
    @DisplayName("obtenerEntidadPorId - Con ID inexistente - Lanza excepción")
    void obtenerEntidadPorId_ConIdInexistente_LanzaExcepcion() {
        // Given
        when(personaRepository.findById("persona999")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            personaService.obtenerEntidadPorId("persona999");
        });

        assertEquals("Persona no encontrada", exception.getMessage());
    }
}
