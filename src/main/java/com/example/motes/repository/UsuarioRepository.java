package com.example.motes.repository;

import com.example.motes.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT DISTINCT u FROM Usuario u WHERE u.id IN (SELECT DISTINCT m.createdBy FROM Mote m)")
    List<Usuario> findUsuariosQueHanCreadoMotes();
}
