package com.example.motes.controller;

import com.example.motes.dto.PersonaRequestDTO;
import com.example.motes.dto.PersonaResponseDTO;
import com.example.motes.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody PersonaRequestDTO request,
            Authentication authentication) {

        try {
            // Validar que el nombre no esté vacío o solo contenga espacios
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("El nombre es requerido"));
            }
            
            // Si se va a crear cuenta, validar email y password
            if (request.getCrearCuenta() != null && request.getCrearCuenta()) {
                if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                    return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("El email es requerido para crear una cuenta"));
                }
                if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                    return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("La contraseña es requerida para crear una cuenta"));
                }
            }
            
            String usuarioEmail = authentication.getName();
            PersonaResponseDTO response = personaService.crear(request, usuarioEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<PersonaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(personaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponseDTO> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }
}
