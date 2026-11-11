package com.example.motes.service;

import com.example.motes.domain.Persona;
import com.example.motes.domain.Usuario;
import com.example.motes.dto.UsuarioDTO;
import com.example.motes.repository.PersonaRepository;
import com.example.motes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonaRepository personaRepository;

    /**
     * Registra un nuevo usuario con el email, password y nombre dados.
     * La contraseña se codifica con el encoder de contraseñas.
     * Se crea una nueva persona con el nombre y email dados y se asocia automáticamente con el usuario.
     * @param email Email del usuario.
     * @param password Contrase a del usuario.
     * @param nombre Nombre del usuario.
     * @return El usuario registrado.
     */
    @Transactional
    public Usuario registrar(String email, String password, String nombre) {
        validarEmail(email);
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setIsAdmin(false);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Crear Persona asociada automáticamente
        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setEmail(email);
        persona.setUserId(usuarioGuardado.getId());
        persona.setCreatedBy(usuarioGuardado.getId()); // La persona es creada por sí misma
        personaRepository.save(persona);

        return usuarioGuardado;
    }

    /**
     * Busca un usuario por su email.
     * @param email Email del usuario.
     * @return El usuario encontrado.
     * @throws RuntimeException Si no se encuentra un usuario con ese email.
     */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Busca un usuario por su id.
     * @param id Id del usuario.
     * @return El usuario encontrado.
     * @throws RuntimeException Si no se encuentra un usuario con ese id.
     */
    public Usuario buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Convierte un usuario a un objeto de transferencia de datos de usuario.
     * @param usuario El usuario a convertir.
     * @return El objeto de transferencia de datos de usuario.
     */
    public UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getIsAdmin(),
                usuario.getCreatedAt()
        );
    }

    /**
     * Valida si la contraseña dada coincide con la contraseña del usuario.
     * @param usuario El usuario cuya contraseña se va a validar.
     * @param password La contraseña a validar.
     * @return True si la contraseña coincide, false en caso contrario.
     */
    public boolean validarPassword(Usuario usuario, String password) {
        return passwordEncoder.matches(password, usuario.getPassword());
    }

    /**
     * Obtener una lista de usuarios que han creado al menos un mote.
     * La lista se devuelve como una lista de objetos de transferencia de datos de usuario.
     * @return Una lista con los usuarios que han creado al menos un mote.
     */
    public List<UsuarioDTO> obtenerCreadores() {
        return usuarioRepository.findUsuariosQueHanCreadoMotes().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Valida si el email dado es válido y no está registrado.
     * Se considera un email válido si tiene el formato "nombre@dominio" y no contiene
     * caracteres especiales ni espacios en blanco. Se considera que un email ya está
     * registrado si ya existe en la base de datos.
     * @param email El email a validar.
     * @throws RuntimeException Si el email no es válido o ya está registrado.
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
