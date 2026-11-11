package com.example.motes.controller;

import com.example.motes.dto.UsuarioDTO;
import com.example.motes.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/creadores")
    public ResponseEntity<List<UsuarioDTO>> obtenerCreadores() {
        List<UsuarioDTO> creadores = usuarioService.obtenerCreadores();
        return ResponseEntity.ok(creadores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable String id) {
        UsuarioDTO usuario = usuarioService.convertirADTO(usuarioService.buscarPorId(id));
        return ResponseEntity.ok(usuario);
    }
}
