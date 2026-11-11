package com.example.motes.config;


import com.example.motes.domain.Mote;
import com.example.motes.domain.Persona;
import com.example.motes.domain.Usuario;
import com.example.motes.domain.Voto;
import com.example.motes.repository.MoteRepository;
import com.example.motes.repository.PersonaRepository;
import com.example.motes.repository.UsuarioRepository;
import com.example.motes.repository.VotoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final MoteRepository moteRepository;
    private final VotoRepository votoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("classpath:data.json")
    private Resource dataResource;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info("Initializing database from data.json...");
            try {
                SeedData seedData = objectMapper.readValue(dataResource.getInputStream(), SeedData.class);

                // 1. Usuarios
                for (UsuarioSeed u : seedData.getUsuarios()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(u.getId());
                    usuario.setEmail(u.getEmail());
                    usuario.setPassword(passwordEncoder.encode(u.getPassword()));
                    usuario.setNombre(u.getNombre());
                    usuario.setIsAdmin(u.isAdmin());
                    usuarioRepository.save(usuario);
                    log.info("Created user: {}", u.getEmail());
                }

                // 2. Personas
                for (PersonaSeed p : seedData.getPersonas()) {
                    Persona persona = new Persona();
                    persona.setId(p.getId());
                    persona.setNombre(p.getNombre());
                    persona.setEmail(p.getEmail());
                    persona.setCreatedBy(p.getCreatedBy());
                    personaRepository.save(persona);
                }
                log.info("Created {} personas", seedData.getPersonas().size());

                // 3. Motes
                for (MoteSeed m : seedData.getMotes()) {
                    Mote mote = new Mote();
                    mote.setId(m.getId());
                    mote.setPersonaId(m.getPersonaId());
                    mote.setPersonaNombre(m.getPersonaNombre());
                    mote.setMote(m.getMote());
                    mote.setDescripcion(m.getDescripcion());
                    mote.setCreatedBy(m.getCreatedBy());
                    //mote.setVotos(m.getVotos());
                    moteRepository.save(mote);
                }
                log.info("Created {} motes", seedData.getMotes().size());

                // 4. Votos
                for (VotoSeed v : seedData.getVotos()) {
                    Voto voto = new Voto();
                    voto.setUsuarioId(v.getUserId());
                    voto.setMoteId(v.getMoteId());
                    votoRepository.save(voto);
                }
                log.info("Created {} votos", seedData.getVotos().size());

                log.info("Database initialization completed!");

            } catch (Exception e) {
                log.error("Error initializing database: {}", e.getMessage(), e);
            }
        } else {
            log.info("Database already contains data, skipping initialization.");
        }
    }

    @Data
    static class SeedData {
        List<UsuarioSeed> usuarios;
        List<PersonaSeed> personas;
        List<MoteSeed> motes;
        List<VotoSeed> votos;
    }

    @Data
    static class UsuarioSeed {
        String id;
        String email;
        String password;
        String nombre;
        boolean isAdmin;
    }

    @Data
    static class PersonaSeed {
        String id;
        String nombre;
        String email;
        String createdBy;
    }

    @Data
    static class MoteSeed {
        String id;
        String personaId;
        String personaNombre;
        String mote;
        String descripcion;
        String createdBy;
        int votos;
    }

    @Data
    static class VotoSeed {
        String userId;
        String moteId;
    }
}
