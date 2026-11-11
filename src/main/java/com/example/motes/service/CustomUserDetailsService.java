package com.example.motes.service;

import com.example.motes.domain.Usuario;
import com.example.motes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Recupera informaci n de un usuario desde la base de datos a partir de su email.

     *
     * @param email el email del usuario a buscar
     * @return el usuario encontrado, o null si no se encontr
     * @throws UsernameNotFoundException si el usuario no se encuentra en la base de datos
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}
