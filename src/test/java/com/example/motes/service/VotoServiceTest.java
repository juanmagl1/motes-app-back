package com.example.motes.service;

import com.example.motes.domain.Voto;
import com.example.motes.dto.VotoResponseDTO;
import com.example.motes.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VotoService Tests")
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private VotoService votoService;

    // ==================== Tests de toggleVoto() ====================

    @Test
    @DisplayName("toggleVoto - Usuario no ha votado - Agrega voto")
    void toggleVoto_UsuarioNoHaVotado_AgregaVoto() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(false);
        when(votoRepository.countByMoteId("mote1")).thenReturn(1);

        // When
        VotoResponseDTO resultado = votoService.toggleVoto("user1", "mote1");

        // Then
        assertTrue(resultado.getVoted());
        assertEquals(1, resultado.getVotos());
        verify(votoRepository).save(any(Voto.class));
        verify(votoRepository, never()).deleteByUsuarioIdAndMoteId(anyString(), anyString());
    }

    @Test
    @DisplayName("toggleVoto - Usuario ya votó - Quita voto")
    void toggleVoto_UsuarioYaVoto_QuitaVoto() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(true);
        when(votoRepository.countByMoteId("mote1")).thenReturn(0);

        // When
        VotoResponseDTO resultado = votoService.toggleVoto("user1", "mote1");

        // Then
        assertFalse(resultado.getVoted());
        assertEquals(0, resultado.getVotos());
        verify(votoRepository).deleteByUsuarioIdAndMoteId("user1", "mote1");
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    @DisplayName("toggleVoto - Múltiples votos - Cuenta correcta")
    void toggleVoto_MultiplesVotos_CuentaCorrecta() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(false);
        when(votoRepository.countByMoteId("mote1")).thenReturn(5);

        // When
        VotoResponseDTO resultado = votoService.toggleVoto("user1", "mote1");

        // Then
        assertEquals(5, resultado.getVotos());
    }

    @Test
    @DisplayName("toggleVoto - CountByMoteId null - Retorna 0")
    void toggleVoto_CountByMoteIdNull_Retorna0() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(false);
        when(votoRepository.countByMoteId("mote1")).thenReturn(null);

        // When
        VotoResponseDTO resultado = votoService.toggleVoto("user1", "mote1");

        // Then
        assertEquals(0, resultado.getVotos());
    }

    // ==================== Tests de hasVotado() ====================

    @Test
    @DisplayName("hasVotado - Usuario ha votado - Retorna true")
    void hasVotado_UsuarioHaVotado_RetornaTrue() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(true);

        // When
        boolean resultado = votoService.hasVotado("user1", "mote1");

        // Then
        assertTrue(resultado);
    }

    @Test
    @DisplayName("hasVotado - Usuario no ha votado - Retorna false")
    void hasVotado_UsuarioNoHaVotado_RetornaFalse() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(false);

        // When
        boolean resultado = votoService.hasVotado("user1", "mote1");

        // Then
        assertFalse(resultado);
    }

    @Test
    @DisplayName("hasVotado - Diferentes usuarios - Comportamiento independiente")
    void hasVotado_DiferentesUsuarios_ComportamientoIndependiente() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(true);
        when(votoRepository.existsByUsuarioIdAndMoteId("user2", "mote1")).thenReturn(false);

        // When
        boolean resultado1 = votoService.hasVotado("user1", "mote1");
        boolean resultado2 = votoService.hasVotado("user2", "mote1");

        // Then
        assertTrue(resultado1);
        assertFalse(resultado2);
    }

    @Test
    @DisplayName("hasVotado - Diferentes motes - Comportamiento independiente")
    void hasVotado_DiferentesMotes_ComportamientoIndependiente() {
        // Given
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote1")).thenReturn(true);
        when(votoRepository.existsByUsuarioIdAndMoteId("user1", "mote2")).thenReturn(false);

        // When
        boolean resultado1 = votoService.hasVotado("user1", "mote1");
        boolean resultado2 = votoService.hasVotado("user1", "mote2");

        // Then
        assertTrue(resultado1);
        assertFalse(resultado2);
    }
}
