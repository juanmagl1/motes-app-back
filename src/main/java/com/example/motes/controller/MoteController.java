package com.example.motes.controller;

import com.example.motes.dto.MoteRequestDTO;
import com.example.motes.dto.MoteResponseDTO;
import com.example.motes.service.MoteService;
import com.example.motes.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MoteController {

    private final MoteService moteService;
    private final UsuarioService usuarioService;

    /**
     * Crea un nuevo mote.
     * @param request Información del mote a crear.
     * @param authentication Información de autenticación del usuario que crea el mote.
     * @return El mote creado.
     * @throws RuntimeException Si no se encuentra la persona asociada al id de request.
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody MoteRequestDTO request,
            Authentication authentication) {
        
        try {
            // Validar que los campos no estén vacíos o solo contengan espacios
            if (request.getPersonaId() == null || request.getPersonaId().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("La persona es requerida"));
            }
            if (request.getMote() == null || request.getMote().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("El mote es requerido"));
            }
            if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("La descripción es requerida"));
            }
            
            // Validar tipo de imagen si se proporciona
            if (request.getImagen() != null && !request.getImagen().isEmpty()) {
                if (!isValidImageFormat(request.getImagen())) {
                    return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("Solo se permiten imágenes con formato: JPG, JPEG, PNG, GIF, SVG, WEBP, BMP, AVIF"));
                }
            }
            
            String usuarioEmail = authentication.getName();
            String usuarioId = usuarioService.buscarPorEmail(usuarioEmail).getId();
            
            MoteResponseDTO response = moteService.crear(request, usuarioId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO(e.getMessage()));
        }
    }

    /**
     * Obtiene una lista de motes.
     *
     * Puede filtrar por nombre de mote, id de persona o id de creador.
     *
     * @param nombreMote Nombre del mote a buscar.
     * @param personaId Id de la persona a la que se asocia el mote a buscar.
     * @param creadorId Id del usuario que creó el mote a buscar.
     * @return Una lista de motes que coinciden con los parámetros de búsqueda.
     */
    @GetMapping
    public ResponseEntity<List<MoteResponseDTO>> obtenerTodos(
            @RequestParam(required = false) String nombreMote,
            @RequestParam(required = false) String personaId,
            @RequestParam(required = false) String creadorId) {
        
        if (nombreMote != null || personaId != null || creadorId != null) {
            return ResponseEntity.ok(moteService.filtrar(nombreMote, personaId, creadorId));
        }
        
        return ResponseEntity.ok(moteService.obtenerTodos());
    }

    /**
     * Obtiene una lista de los 5 motes con más votos, ordenados por cantidad de votos en orden descendente.
     * @return Una lista de los 5 motes con más votos.
     */
    @GetMapping("/top5")
    public ResponseEntity<List<MoteResponseDTO>> obtenerTop5() {
        return ResponseEntity.ok(moteService.obtenerTop5());
    }

    @GetMapping("/destacado/{personaId}")
    public ResponseEntity<MoteResponseDTO> obtenerMoteDestacado(@PathVariable String personaId) {
        MoteResponseDTO moteDestacado = moteService.obtenerMoteDestacado(personaId);
        if (moteDestacado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(moteDestacado);
    }

    /**
     * Obtiene un mote por su id.
     * @param id Id del mote a obtener.
     * @return El mote correspondiente al id.
     * @throws RuntimeException Si no se encuentra el mote asociado al id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MoteResponseDTO> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(moteService.obtenerPorId(id));
    }

    /**
     * Actualiza un mote existente.
     * @param id Id del mote a actualizar.
     * @param request Información del mote a actualizar.
     * @param authentication Información de autenticación del usuario que hace la actualización.
     * @return El mote actualizado.
     * @throws RuntimeException Si no se encuentra el mote asociado al id,
     *         o si el usuario no es el creador ni es admin.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody MoteRequestDTO request,
            Authentication authentication) {
        
        try {
            // Validar que los campos no estén vacíos o solo contengan espacios
            if (request.getPersonaId() == null || request.getPersonaId().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("La persona es requerida"));
            }
            if (request.getMote() == null || request.getMote().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("El mote es requerido"));
            }
            if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("La descripción es requerida"));
            }
            
            // Validar tipo de imagen si se proporciona
            if (request.getImagen() != null && !request.getImagen().isEmpty()) {
                if (!isValidImageFormat(request.getImagen())) {
                    return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO("Solo se permiten imágenes con formato: JPG, JPEG, PNG, GIF, SVG, WEBP, BMP, AVIF"));
                }
            }
            
            String usuarioEmail = authentication.getName();
            var usuario = usuarioService.buscarPorEmail(usuarioEmail);
            
            MoteResponseDTO response = moteService.actualizar(id, request, usuario.getId(), usuario.getIsAdmin());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new com.example.motes.dto.ErrorResponseDTO(e.getMessage()));
        }
    }

    /**
     * Elimina un mote existente.
     * @param id Id del mote a eliminar.
     * @param authentication Información de autenticación del usuario que hace la eliminación.
     * @return No contenido.
     * @throws RuntimeException Si no se encuentra el mote asociado al id,
     *         o si el usuario no es el creador ni es admin.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id,
            Authentication authentication) {
        
        String usuarioEmail = authentication.getName();
        var usuario = usuarioService.buscarPorEmail(usuarioEmail);
        
        moteService.eliminar(id, usuario.getId(), usuario.getIsAdmin());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Valida que la imagen en base64 tenga un formato válido
     * Formatos permitidos: JPG, JPEG, PNG, GIF, SVG, WEBP, BMP
     */
    private boolean isValidImageFormat(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return false;
        }
        
        // Extraer el tipo MIME del prefijo data:image/...;base64,
        String[] parts = base64Image.split(",");
        if (parts.length < 2) {
            return false;
        }
        
        String header = parts[0];
        // Verificar que comience con data:image/
        if (!header.startsWith("data:image/")) {
            return false;
        }
        
        // Extraer el tipo de imagen
        String imageType = header.substring("data:image/".length(), header.indexOf(";"));
        
        // Lista de formatos permitidos
        java.util.List<String> allowedFormats = java.util.Arrays.asList(
            "jpg", "jpeg", "png", "gif", "svg+xml", "webp", "bmp", "avif"
        );
        
        return allowedFormats.contains(imageType.toLowerCase());
    }
}
