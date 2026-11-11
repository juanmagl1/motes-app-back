package com.example.motes.service;

import com.example.motes.domain.Voto;
import com.example.motes.dto.VotoResponseDTO;
import com.example.motes.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VotoService {

    private final VotoRepository votoRepository;

    /**
     * Toggles a vote from a user to a mote.
     * If the user has already voted, it removes the vote.
     * If the user has not voted, it adds a vote.
     * @param usuarioId the user id
     * @param moteId the mote id
     * @return a response containing information about whether the vote was toggled and the current number of votes
     */
    @Transactional
    public VotoResponseDTO toggleVoto(String usuarioId, String moteId) {
        boolean yaVoto = votoRepository.existsByUsuarioIdAndMoteId(usuarioId, moteId);

        if (yaVoto) {
            // Quitar voto
            votoRepository.deleteByUsuarioIdAndMoteId(usuarioId, moteId);
        } else {
            // Agregar voto
            Voto voto = new Voto(usuarioId, moteId);
            votoRepository.save(voto);
        }

        Integer votosActuales = votoRepository.countByMoteId(moteId);
        return new VotoResponseDTO(!yaVoto, votosActuales != null ? votosActuales : 0);
    }

    /**
     * Checks if a user has voted for a mote.
     * @param usuarioId the user id
     * @param moteId the mote id
     * @return true if the user has voted, false otherwise
     */
    public boolean hasVotado(String usuarioId, String moteId) {
        return votoRepository.existsByUsuarioIdAndMoteId(usuarioId, moteId);
    }
}
