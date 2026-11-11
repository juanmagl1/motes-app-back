package com.example.motes.service;

import com.example.motes.domain.Persona;
import com.example.motes.domain.Usuario;
import com.example.motes.dto.PersonaRequestDTO;
import com.example.motes.dto.PersonaResponseDTO;
import com.example.motes.repository.PersonaRepository;
import com.example.motes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea una nueva persona con el nombre y email dados.
     * Si se solicita crear cuenta y hay email/password, crea un nuevo usuario con la información dada.
     * Lanza una excepción si ya existe una persona con ese nombre.
     * @param request Información de la persona a crear
     * @param creadorId Id del usuario que crea la persona
     * @return La persona creada
     * @throws RuntimeException Si ya existe una persona con ese nombre
     */
    @Transactional
    public PersonaResponseDTO crear(PersonaRequestDTO request, String creadorId) {
        if (!personaRepository.findByNombre(request.getNombre()).isEmpty()) {
            throw new RuntimeException("Ya existe una persona con ese nombre");
        }
        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setEmail(request.getEmail());
        persona.setCreatedBy(creadorId);

        // Si se solicita crear cuenta y hay email/password
        if (Boolean.TRUE.equals(request.getCrearCuenta()) 
            && request.getEmail() != null 
            && request.getPassword() != null) {
            
            try {
                validarEmail(request.getEmail());
                Usuario nuevoUsuario = usuarioService.registrar(
                    request.getEmail(), 
                    request.getPassword(), 
                    request.getNombre()
                );
                persona.setUserId(nuevoUsuario.getId());
            } catch (RuntimeException e) {
                // Relanzar la excepción para que el controller la maneje
                throw new RuntimeException(e.getMessage());
            }
        }

        Persona personaGuardada = personaRepository.save(persona);
        return convertirADTO(personaGuardada);
    }

    /**
     * Obtener todas las personas.
     * @return Una lista con todas las personas.
     */
    public List<PersonaResponseDTO> obtenerTodas() {
        return personaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una persona por su id.
     * Lanza una excepción si no se encuentra una persona con ese id.
     * @param id Id de la persona a obtener
     * @return La persona obtenida
     * @throws RuntimeException Si no se encuentra una persona con ese id
     */
    public PersonaResponseDTO obtenerPorId(String id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        return convertirADTO(persona);
    }

    /**
     * Obtiene una persona por su id.
     * @param id Id de la persona a obtener
     * @return La persona obtenida
     * @throws RuntimeException Si no se encuentra una persona con ese id
     */
    public Persona obtenerEntidadPorId(String id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
    }

    /**
     * Convierte un objeto Persona en un objeto PersonaResponseDTO.
     * @param persona El objeto Persona a convertir.
     * @return Un objeto PersonaResponseDTO con la información correspondiente.
     */
    private PersonaResponseDTO convertirADTO(Persona persona) {
        return new PersonaResponseDTO(
                persona.getId(),
                persona.getNombre(),
                persona.getEmail(),
                persona.getUserId(),
                persona.getCreatedBy(),
                persona.getCreatedAt()
        );
    }

    /**
     * Valida si un email es válido.
     * Un email es válido si:
     * - Tiene una sola '@'
     * - La parte izquierda no contiene caracteres especiales
     * - La parte derecha es uno de los tipos de correo admitidos
     * - No se encuentra registrado en la base de datos
     * @param email El email a validar
     * @throws RuntimeException Si el email no es válido
     */
    private void validarEmail(String email){
        List<String> tiposCorreos= List.of("gmail.com","hotmail.com","yahoo.com","outlook.com");
        List<String> caracteresEspeciales= List.of(" ","!","#","$","%","^","&","*","(",")","+","=","{","}","[","]","|","\\",":",";","\"","'","<",">",",","?","/");
        String[] correo=email.trim().split("@");
        if (correo.length>2 || correo[0].isEmpty() || correo[1].isEmpty() || !tiposCorreos.contains(correo[1]) || caracteresEspeciales.contains(correo[0]) || caracteresEspeciales.contains(correo[1])) {
            throw new RuntimeException("El formato de email no es válido");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

    }
}
