package com.example.motes.controller;

import com.example.motes.domain.Usuario;
import com.example.motes.dto.*;
import com.example.motes.security.JwtService;
import com.example.motes.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    /**
     * Registrar un nuevo usuario.
     * @param request datos de registro del usuario.
     * @return respuesta con el token de acceso y el usuario registrado.
     * @throws RuntimeException si el registro falla.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        try {
            // Validar que los campos no estén vacíos o solo contengan espacios
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new ErrorResponseDTO("El email es requerido"));
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new ErrorResponseDTO("La contraseña es requerida"));
            }
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new ErrorResponseDTO("El nombre es requerido"));
            }
            
            Usuario usuario = usuarioService.registrar(request.getEmail(), request.getPassword(), request.getNombre());
            String token = jwtService.generateToken(usuario.getEmail());
            UsuarioDTO usuarioDTO = usuarioService.convertirADTO(usuario);
            
            return ResponseEntity.ok(new LoginResponseDTO(token, usuarioDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new ErrorResponseDTO(e.getMessage()));
        }
    }

    /**
     * Loguear a un usuario existente.
     * @param request datos de login del usuario.
     * @return respuesta con el token de acceso y el usuario logueado.
     * @throws RuntimeException si el login falla.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            // Validar que los campos no estén vacíos o solo contengan espacios
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            
            Usuario usuario = usuarioService.buscarPorEmail(request.getEmail());
            
            if (!usuarioService.validarPassword(usuario, request.getPassword())) {
                return ResponseEntity.status(401).build();
            }
            
            String token = jwtService.generateToken(usuario.getEmail());
            UsuarioDTO usuarioDTO = usuarioService.convertirADTO(usuario);
            
            return ResponseEntity.ok(new LoginResponseDTO(token, usuarioDTO));
        } catch (RuntimeException e) {
            // Usuario no encontrado - retornar 401 igual que password incorrecta
            // Por seguridad, no revelamos si el usuario existe o no
            return ResponseEntity.status(401).build();
        }
    }
}
