package com.example.motes.repository;

import com.example.motes.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, String> {
    Optional<Persona> findByEmail(String email);
    List<Persona> findByNombre(String nombre);

}
