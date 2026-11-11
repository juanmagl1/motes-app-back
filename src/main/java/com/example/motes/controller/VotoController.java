package com.example.motes.controller;

import com.example.motes.dto.VotoRequestDTO;
import com.example.motes.dto.VotoResponseDTO;
import com.example.motes.service.UsuarioService;
import com.example.motes.service.VotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VotoController {

    private final VotoService votoService;
    private final UsuarioService usuarioService;

    @PostMapping("/toggle")
    public ResponseEntity<VotoResponseDTO> toggleVoto(
            @RequestBody VotoRequestDTO request,
            Authentication authentication) {
        
        String usuarioEmail = authentication.getName();
        String usuarioId = usuarioService.buscarPorEmail(usuarioEmail).getId();
        
        VotoResponseDTO response = votoService.toggleVoto(usuarioId, request.getMoteId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{moteId}")
    public ResponseEntity<Boolean> hasVotado(
            @PathVariable String moteId,
            Authentication authentication) {
        
        String usuarioEmail = authentication.getName();
        String usuarioId = usuarioService.buscarPorEmail(usuarioEmail).getId();
        
        boolean hasVotado = votoService.hasVotado(usuarioId, moteId);
        return ResponseEntity.ok(hasVotado);
    }
}
