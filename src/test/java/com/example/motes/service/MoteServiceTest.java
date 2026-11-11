package com.example.motes.service;

import com.example.motes.domain.Mote;
import com.example.motes.domain.Persona;
import com.example.motes.dto.MoteRequestDTO;
import com.example.motes.dto.MoteResponseDTO;
import com.example.motes.repository.MoteRepository;
import com.example.motes.repository.VotoRepository;
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
@DisplayName("MoteService Tests")
class MoteServiceTest {

    @Mock
    private MoteRepository moteRepository;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PersonaService personaService;

    @InjectMocks
    private MoteService moteService;

    private Mote moteTest;
    private MoteRequestDTO requestDTO;
    private Persona personaTest;

    @BeforeEach
    void setUp() {
        personaTest = new Persona();
        personaTest.setId("persona1");
        personaTest.setNombre("Test Persona");

        moteTest = new Mote();
        moteTest.setId("mote1");
        moteTest.setPersonaId("persona1");
        moteTest.setPersonaNombre("Test Persona");
        moteTest.setMote("El Rayo");
        moteTest.setDescripcion("Test descripcion");
        moteTest.setCreatedBy("user1");
        moteTest.setCreatedAt(LocalDateTime.now());

        requestDTO = new MoteRequestDTO();
        requestDTO.setPersonaId("persona1");
        requestDTO.setMote("El Rayo");
        requestDTO.setDescripcion("Test descripcion");
    }

    // ==================== Tests de crear() ====================

    @Test
    @DisplayName("crear - Con datos válidos - Retorna mote creado")
    void crear_ConDatosValidos_RetornaMoteCreado() {
        // Given
        when(personaService.obtenerEntidadPorId("persona1")).thenReturn(personaTest);
        when(moteRepository.save(any(Mote.class))).thenReturn(moteTest);
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When
        MoteResponseDTO resultado = moteService.crear(requestDTO, "user1");

        // Then
        assertNotNull(resultado);
        assertEquals("El Rayo", resultado.getMote());
        assertEquals("Test Persona", resultado.getPersonaNombre());
        verify(moteRepository).save(any(Mote.class));
    }

    @Test
    @DisplayName("crear - Con imagen base64 - Guarda imagen")
    void crear_ConImagenBase64_GuardaImagen() {
        // Given
        requestDTO.setImagen("data:image/png;base64,iVBORw0KGgoAAAANS...");
        when(personaService.obtenerEntidadPorId("persona1")).thenReturn(personaTest);
        when(moteRepository.save(any(Mote.class))).thenReturn(moteTest);
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When
        MoteResponseDTO resultado = moteService.crear(requestDTO, "user1");

        // Then
        assertNotNull(resultado);
        verify(moteRepository).save(any(Mote.class));
    }

    @Test
    @DisplayName("crear - Con persona inexistente - Lanza excepción")
    void crear_ConPersonaInexistente_LanzaExcepcion() {
        // Given
        when(personaService.obtenerEntidadPorId("persona1"))
                .thenThrow(new RuntimeException("Persona no encontrada"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            moteService.crear(requestDTO, "user1");
        });
    }

    // ==================== Tests de actualizar() ====================

    @Test
    @DisplayName("actualizar - Con datos válidos - Retorna mote actualizado")
    void actualizar_ConDatosValidos_RetornaMoteActualizado() {
        // Given
        requestDTO.setMote("Nuevo Mote");
        requestDTO.setDescripcion("Nueva descripcion");
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(moteRepository.save(any(Mote.class))).thenReturn(moteTest);
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When (usuario creador, no admin necesario)
        MoteResponseDTO resultado = moteService.actualizar("mote1", requestDTO, "user1", false);

        // Then
        assertNotNull(resultado);
        verify(moteRepository).save(any(Mote.class));
    }

    @Test
    @DisplayName("actualizar - Con mote inexistente - Lanza excepción")
    void actualizar_ConMoteInexistente_LanzaExcepcion() {
        // Given
        when(moteRepository.findById("mote999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            moteService.actualizar("mote999", requestDTO, "user1", false);
        });
    }

    @Test
    @DisplayName("actualizar - Sin permiso - Lanza excepción")
    void actualizar_SinPermiso_LanzaExcepcion() {
        // Given
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));

        // When & Then (usuario distinto y no admin)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            moteService.actualizar("mote1", requestDTO, "user999", false);
        });

        assertEquals("No tienes permiso para editar este mote", exception.getMessage());
    }

    @Test
    @DisplayName("actualizar - Admin puede editar mote de otro usuario")
    void actualizar_AdminPuedeEditarMoteDeOtroUsuario() {
        // Given
        requestDTO.setMote("Mote Admin");
        requestDTO.setDescripcion("Desc admin");
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(moteRepository.save(any(Mote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When (usuario distinto pero admin = true)
        MoteResponseDTO resultado = moteService.actualizar("mote1", requestDTO, "otroUser", true);

        // Then
        assertNotNull(resultado);
        verify(moteRepository).save(any(Mote.class));
    }

    @Test
    @DisplayName("actualizar - Sin imagen - No actualiza imagen")
    void actualizar_SinImagen_NoActualizaImagen() {
        // Given
        requestDTO.setImagen(null);
        moteTest.setImagen("imagen_original");
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(moteRepository.save(any(Mote.class))).thenReturn(moteTest);
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When
        moteService.actualizar("mote1", requestDTO, "user1", false);

        // Then
        verify(moteRepository).save(argThat(mote ->
                mote.getImagen() != null && mote.getImagen().equals("imagen_original")
        ));
    }

    @Test
    @DisplayName("actualizar - Con nueva imagen - Actualiza imagen")
    void actualizar_ConNuevaImagen_ActualizaImagen() {
        // Given
        moteTest.setImagen("imagen_original");
        requestDTO.setImagen("nueva_imagen_base64");

        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(moteRepository.save(any(Mote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When
        MoteResponseDTO resultado = moteService.actualizar("mote1", requestDTO, "user1", false);

        // Then
        assertNotNull(resultado);
        verify(moteRepository).save(argThat(mote ->
                "nueva_imagen_base64".equals(mote.getImagen())
        ));
        assertEquals("nueva_imagen_base64", resultado.getImagen());
    }

    @Test
    @DisplayName("actualizar - Cambiando persona - Actualiza persona y nombre")
    void actualizar_CambiandoPersona_ActualizaPersonaYNombre() {
        // Given: nueva persona distinta a la actual
        Persona nuevaPersona = new Persona();
        nuevaPersona.setId("persona2");
        nuevaPersona.setNombre("Nueva Persona");

        requestDTO.setPersonaId("persona2");          // distinto de "persona1"
        requestDTO.setMote("Mote con nueva persona");
        requestDTO.setDescripcion("Desc nueva persona");

        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(personaService.obtenerEntidadPorId("persona2")).thenReturn(nuevaPersona);
        when(moteRepository.save(any(Mote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(votoRepository.countByMoteId(anyString())).thenReturn(0);

        // When
        MoteResponseDTO resultado = moteService.actualizar("mote1", requestDTO, "user1", false);

        // Then
        assertNotNull(resultado);
        verify(personaService).obtenerEntidadPorId("persona2");
        verify(moteRepository).save(argThat(m ->
                "persona2".equals(m.getPersonaId()) &&
                        "Nueva Persona".equals(m.getPersonaNombre())
        ));
        assertEquals("Nueva Persona", resultado.getPersonaNombre());
    }

    // ==================== Tests de eliminar() ====================

    @Test
    @DisplayName("eliminar - Con ID válido - Elimina mote")
    void eliminar_ConIdValido_EliminaMote() {
        // Given
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));

        // When (usuario creador, no admin necesario)
        moteService.eliminar("mote1", "user1", false);

        // Then
        verify(moteRepository).deleteById("mote1");
    }

    @Test
    @DisplayName("eliminar - Con mote inexistente - Lanza excepción")
    void eliminar_ConMoteInexistente_LanzaExcepcion() {
        // Given
        when(moteRepository.findById("mote999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            moteService.eliminar("mote999", "user1", false);
        });
    }

    @Test
    @DisplayName("eliminar - Sin permiso - Lanza excepción")
    void eliminar_SinPermiso_LanzaExcepcion() {
        // Given
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));

        // When & Then (usuario distinto y no admin)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            moteService.eliminar("mote1", "user999", false);
        });

        assertEquals("No tienes permiso para eliminar este mote", exception.getMessage());
    }

    // ==================== Tests de obtenerTodos() ====================

    @Test
    @DisplayName("obtenerTodos - Con motes existentes - Retorna lista ordenada")
    void obtenerTodos_ConMotesExistentes_RetornaListaOrdenada() {
        // Given
        Mote mote2 = new Mote();
        mote2.setId("mote2");
        mote2.setMote("Otro Mote");
        mote2.setPersonaId("persona1");
        mote2.setPersonaNombre("Test Persona");
        mote2.setDescripcion("Desc");
        mote2.setCreatedBy("user1");

        when(moteRepository.findAll()).thenReturn(Arrays.asList(moteTest, mote2));
        when(votoRepository.countByMoteId("mote1")).thenReturn(5);
        when(votoRepository.countByMoteId("mote2")).thenReturn(3);

        // When
        List<MoteResponseDTO> resultado = moteService.obtenerTodos();

        // Then
        assertEquals(2, resultado.size());
        assertEquals(5, resultado.get(0).getVotos());
        assertEquals(3, resultado.get(1).getVotos());
    }

    @Test
    @DisplayName("obtenerTodos - Sin motes - Retorna lista vacía")
    void obtenerTodos_SinMotes_RetornaListaVacia() {
        // Given
        when(moteRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<MoteResponseDTO> resultado = moteService.obtenerTodos();

        // Then
        assertTrue(resultado.isEmpty());
    }

    // ==================== Tests de obtenerPorId() ====================

    @Test
    @DisplayName("obtenerPorId - Con ID existente - Retorna mote")
    void obtenerPorId_ConIdExistente_RetornaMote() {
        // Given
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(votoRepository.countByMoteId("mote1")).thenReturn(3);

        // When
        MoteResponseDTO resultado = moteService.obtenerPorId("mote1");

        // Then
        assertNotNull(resultado);
        assertEquals("El Rayo", resultado.getMote());
        assertEquals(3, resultado.getVotos());
    }

    @Test
    @DisplayName("obtenerPorId - Con ID inexistente - Lanza excepción")
    void obtenerPorId_ConIdInexistente_LanzaExcepcion() {
        // Given
        when(moteRepository.findById("mote999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            moteService.obtenerPorId("mote999");
        });
    }

    @Test
    @DisplayName("obtenerPorId - Con votos null - Usa 0 por defecto")
    void obtenerPorId_ConVotosNull_UsaCeroPorDefecto() {
        // Given
        when(moteRepository.findById("mote1")).thenReturn(Optional.of(moteTest));
        when(votoRepository.countByMoteId("mote1")).thenReturn(null);

        // When
        MoteResponseDTO resultado = moteService.obtenerPorId("mote1");

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.getVotos());
    }

    // ==================== Tests de obtenerTop5() ====================

    @Test
    @DisplayName("obtenerTop5 - Con muchos motes - Retorna solo 5")
    void obtenerTop5_ConMuchosMotes_RetornaSolo5() {
        // Given
        List<Mote> motes = Arrays.asList(
                crearMote("m1", 10),
                crearMote("m2", 8),
                crearMote("m3", 6),
                crearMote("m4", 4),
                crearMote("m5", 2),
                crearMote("m6", 1)
        );

        when(moteRepository.findAll()).thenReturn(motes);
        for (Mote m : motes) {
            when(votoRepository.countByMoteId(m.getId()))
                    .thenReturn(Integer.parseInt(m.getId().substring(1)));
        }

        // When
        List<MoteResponseDTO> resultado = moteService.obtenerTop5();

        // Then
        assertEquals(5, resultado.size());
    }

    @Test
    @DisplayName("obtenerTop5 - Solo motes con votos - Excluye cero votos")
    void obtenerTop5_SoloMotesConVotos_ExcluyeCeroVotos() {
        // Given
        List<Mote> motes = Arrays.asList(
                crearMote("m1", 5),
                crearMote("m2", 0),
                crearMote("m3", 3)
        );

        when(moteRepository.findAll()).thenReturn(motes);
        when(votoRepository.countByMoteId("m1")).thenReturn(5);
        when(votoRepository.countByMoteId("m2")).thenReturn(0);
        when(votoRepository.countByMoteId("m3")).thenReturn(3);

        // When
        List<MoteResponseDTO> resultado = moteService.obtenerTop5();

        // Then
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(m -> m.getVotos() > 0));
    }

    @Test
    @DisplayName("obtenerTop5 - Sin motes - Retorna lista vacía")
    void obtenerTop5_SinMotes_RetornaListaVacia() {
        // Given
        when(moteRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<MoteResponseDTO> resultado = moteService.obtenerTop5();

        // Then
        assertTrue(resultado.isEmpty());
    }

    // ==================== Tests de filtrar() ====================

    @Test
    @DisplayName("filtrar - Por nombre - Retorna filtrados")
    void filtrar_PorNombre_RetornaFiltrados() {
        // Given
        when(moteRepository.findByFilters("Rayo", null, null))
                .thenReturn(Arrays.asList(moteTest));
        when(votoRepository.countByMoteId(anyString())).thenReturn(3);

        // When
        List<MoteResponseDTO> resultado = moteService.filtrar("Rayo", null, null);

        // Then
        assertEquals(1, resultado.size());
        verify(moteRepository).findByFilters("Rayo", null, null);
    }

    @Test
    @DisplayName("filtrar - Por persona - Retorna filtrados")
    void filtrar_PorPersona_RetornaFiltrados() {
        // Given
        when(moteRepository.findByFilters(null, "persona1", null))
                .thenReturn(Arrays.asList(moteTest));
        when(votoRepository.countByMoteId(anyString())).thenReturn(3);

        // When
        List<MoteResponseDTO> resultado = moteService.filtrar(null, "persona1", null);

        // Then
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("filtrar - Por creador - Retorna filtrados")
    void filtrar_PorCreador_RetornaFiltrados() {
        // Given
        when(moteRepository.findByFilters(null, null, "user1"))
                .thenReturn(Arrays.asList(moteTest));
        when(votoRepository.countByMoteId(anyString())).thenReturn(3);

        // When
        List<MoteResponseDTO> resultado = moteService.filtrar(null, null, "user1");

        // Then
        assertEquals(1, resultado.size());
    }

    // ==================== Tests de obtenerMoteDestacado() ====================

    @Test
    @DisplayName("obtenerMoteDestacado - Con motes existentes - Retorna más votado")
    void obtenerMoteDestacado_ConMotesExistentes_RetornaMasVotado() {
        // Given
        when(moteRepository.findByFilters(null, "persona1", null))
                .thenReturn(Arrays.asList(moteTest));
        when(votoRepository.countByMoteId("mote1")).thenReturn(5);

        // When
        MoteResponseDTO resultado = moteService.obtenerMoteDestacado("persona1");

        // Then
        assertNotNull(resultado);
        assertEquals("El Rayo", resultado.getMote());
    }

    @Test
    @DisplayName("obtenerMoteDestacado - Sin motes - Retorna null")
    void obtenerMoteDestacado_SinMotes_RetornaNull() {
        // Given
        when(moteRepository.findByFilters(null, "persona1", null))
                .thenReturn(Collections.emptyList());

        // When
        MoteResponseDTO resultado = moteService.obtenerMoteDestacado("persona1");

        // Then
        assertNull(resultado);
    }

    // ==================== Métodos auxiliares ====================

    private Mote crearMote(String id, int votos) {
        Mote mote = new Mote();
        mote.setId(id);
        mote.setMote("Mote " + id);
        mote.setPersonaId("persona1");
        mote.setPersonaNombre("Test");
        mote.setDescripcion("Desc");
        mote.setCreatedBy("user1");
        mote.setCreatedAt(LocalDateTime.now());
        return mote;
    }
}
