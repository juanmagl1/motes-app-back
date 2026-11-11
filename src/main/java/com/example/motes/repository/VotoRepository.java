package com.example.motes.repository;

import com.example.motes.domain.Voto;
import com.example.motes.domain.VotoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoRepository extends JpaRepository<Voto, VotoId> {
    
    boolean existsByUsuarioIdAndMoteId(String usuarioId, String moteId);
    
    void deleteByUsuarioIdAndMoteId(String usuarioId, String moteId);
    
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.moteId = :moteId")
    Integer countByMoteId(@Param("moteId") String moteId);
}
