package com.example.motes.service;

import com.example.motes.domain.Usuario;
import com.example.motes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId("user1");
        usuarioTest.setEmail("test@test.com");
        usuarioTest.setPassword("$2a$10$encodedPassword");
        usuarioTest.setNombre("Test User");
        usuarioTest.setIsAdmin(false);
    }

    @Test
    @DisplayName("loadUserByUsername - Con usuario existente - Retorna UserDetails")
    void loadUserByUsername_ConUsuarioExistente_RetornaUserDetails() {
        // Given
        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuarioTest));

        // When
        UserDetails resultado = customUserDetailsService.loadUserByUsername("test@test.com");

        // Then
        assertNotNull(resultado);
        assertEquals("test@test.com", resultado.getUsername());
        assertEquals("$2a$10$encodedPassword", resultado.getPassword());
        assertTrue(resultado.getAuthorities().isEmpty()); // Authorities vacío por defecto
    }

    @Test
    @DisplayName("loadUserByUsername - Con usuario inexistente - Lanza UsernameNotFoundException")
    void loadUserByUsername_ConUsuarioInexistente_LanzaUsernameNotFoundException() {
        // Given
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("noexiste@test.com");
        });
    }

    @Test
    @DisplayName("loadUserByUsername - UserDetails tiene datos correctos")
    void loadUserByUsername_UserDetailsTieneDatosCorrectos() {
        // Given
        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuarioTest));

        // When
        UserDetails resultado = customUserDetailsService.loadUserByUsername("test@test.com");

        // Then
        assertEquals(usuarioTest.getEmail(), resultado.getUsername());
        assertEquals(usuarioTest.getPassword(), resultado.getPassword());
        assertTrue(resultado.isEnabled());
        assertTrue(resultado.isAccountNonExpired());
        assertTrue(resultado.isAccountNonLocked());
        assertTrue(resultado.isCredentialsNonExpired());
    }
}
