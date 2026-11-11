package com.example.motes.repository;

import com.example.motes.domain.Mote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoteRepository extends JpaRepository<Mote, String> {
    
    List<Mote> findByPersonaId(String personaId);
    
    List<Mote> findByCreatedBy(String createdBy);
    
    @Query("SELECT m FROM Mote m WHERE " +
           "(:nombreMote IS NULL OR LOWER(m.mote) LIKE LOWER(CONCAT('%', :nombreMote, '%'))) AND " +
           "(:personaId IS NULL OR m.personaId = :personaId) AND " +
           "(:creadorId IS NULL OR m.createdBy = :creadorId)")
    List<Mote> findByFilters(
        @Param("nombreMote") String nombreMote,
        @Param("personaId") String personaId,
        @Param("creadorId") String creadorId
    );
}
