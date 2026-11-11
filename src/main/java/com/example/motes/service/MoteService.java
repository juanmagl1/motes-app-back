package com.example.motes.service;

import com.example.motes.domain.Mote;
import com.example.motes.domain.Persona;
import com.example.motes.dto.MoteRequestDTO;
import com.example.motes.dto.MoteResponseDTO;
import com.example.motes.repository.MoteRepository;
import com.example.motes.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoteService {

    private final MoteRepository moteRepository;
    private final VotoRepository votoRepository;
    private final PersonaService personaService;

    /**
     * Crea un nuevo mote.
     * @param request Información del mote a crear.
     * @param creadorId Id del usuario que crea el mote.
     * @return El mote creado.
     * @throws RuntimeException Si no se encuentra la persona asociada al id de request.
     */
    @Transactional
    public MoteResponseDTO crear(MoteRequestDTO request, String creadorId) {
        Persona persona = personaService.obtenerEntidadPorId(request.getPersonaId());

        Mote mote = new Mote();
        mote.setPersonaId(request.getPersonaId());
        mote.setPersonaNombre(persona.getNombre());
        mote.setMote(request.getMote());
        mote.setDescripcion(request.getDescripcion());
        mote.setImagen(request.getImagen());
        mote.setCreatedBy(creadorId);

        Mote moteGuardado = moteRepository.save(mote);
        return convertirADTO(moteGuardado);
    }

    /**
     * Actualiza un mote existente.
     * @param id Id del mote a actualizar.
     * @param request Información del mote a actualizar.
     * @param usuarioId Id del usuario que hace la actualización.
     * @param isAdmin Si el usuario es admin.
     * @return El mote actualizado.
     * @throws RuntimeException Si no se encuentra el mote asociado al id,
     *         o si el usuario no es el creador ni es admin.
     */
    @Transactional
    public MoteResponseDTO actualizar(String id, MoteRequestDTO request, String usuarioId, boolean isAdmin) {
        Mote mote = moteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mote no encontrado"));

        // Verificar que el usuario es el creador O es admin
        if (!mote.getCreatedBy().equals(usuarioId) && !isAdmin) {
            throw new RuntimeException("No tienes permiso para editar este mote");
        }

        // Actualizar persona si ha cambiado
        if (request.getPersonaId() != null && !request.getPersonaId().equals(mote.getPersonaId())) {
            Persona persona = personaService.obtenerEntidadPorId(request.getPersonaId());
            mote.setPersonaId(request.getPersonaId());
            mote.setPersonaNombre(persona.getNombre());
        }

        mote.setMote(request.getMote());
        mote.setDescripcion(request.getDescripcion());
        if (request.getImagen() != null) {
            mote.setImagen(request.getImagen());
        }

        Mote moteActualizado = moteRepository.save(mote);
        return convertirADTO(moteActualizado);
    }

    /**
     * Elimina un mote.
     * @param id Id del mote a eliminar.
     * @param usuarioId Id del usuario que hace la eliminación.
     * @param isAdmin Si el usuario es admin.
     * @throws RuntimeException Si no se encuentra el mote asociado al id,
     *         o si el usuario no es el creador ni es admin.
     */
    @Transactional
    public void eliminar(String id, String usuarioId, boolean isAdmin) {
        Mote mote = moteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mote no encontrado"));

        // Verificar que el usuario es el creador O es admin
        if (!mote.getCreatedBy().equals(usuarioId) && !isAdmin) {
            throw new RuntimeException("No tienes permiso para eliminar este mote");
        }

        moteRepository.deleteById(id);
    }

    /**
     * Obtiene una lista de todos los motes ordenados por cantidad de votos en orden descendente.
     * @return Una lista de motes con su información correspondiente.
     */
    public List<MoteResponseDTO> obtenerTodos() {
        return moteRepository.findAll().stream()
                .map(this::convertirADTO)
                .sorted(Comparator.comparing(MoteResponseDTO::getVotos).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un mote por su id.
     * @param id Id del mote a obtener.
     * @return El mote correspondiente al id.
     * @throws RuntimeException Si no se encuentra el mote asociado al id.
     */
    public MoteResponseDTO obtenerPorId(String id) {
        Mote mote = moteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mote no encontrado"));
        return convertirADTO(mote);
    }

    /**
     * Obtiene una lista de los 5 motes con más votos, ordenados por cantidad de votos en orden descendente.
     * @return Una lista de los 5 motes con más votos.
     */
    public List<MoteResponseDTO> obtenerTop5() {
        return moteRepository.findAll().stream()
                .map(this::convertirADTO)
                .filter(mote -> mote.getVotos() > 0)
                .sorted(Comparator.comparing(MoteResponseDTO::getVotos).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Filtra los motes por nombre, personaId o creadorId y devuelve una lista de motes ordenados por cantidad de votos en orden descendente.
     * @param nombreMote El nombre del mote a filtrar.
     * @param personaId El id de la persona a filtrar.
     * @param creadorId El id del creador a filtrar.
     * @return Una lista de motes con su información correspondiente, ordenados por cantidad de votos en orden descendente.
     */
    public List<MoteResponseDTO> filtrar(String nombreMote, String personaId, String creadorId) {
        return moteRepository.findByFilters(nombreMote, personaId, creadorId).stream()
                .map(this::convertirADTO)
                .sorted(Comparator.comparing(MoteResponseDTO::getVotos).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el mote destacado de una persona.
     * @param personaId El id de la persona a obtener el mote destacado.
     * @return El mote destacado de la persona, o null si no se encuentra.
     */
    public MoteResponseDTO obtenerMoteDestacado(String personaId) {
        return moteRepository.findByFilters(null, personaId, null).stream()
                .map(this::convertirADTO)
                .sorted(Comparator.comparing(MoteResponseDTO::getVotos).reversed()
                        .thenComparing(MoteResponseDTO::getCreatedAt))
                .findFirst()
                .orElse(null);
    }

    /**
     * Convierte un objeto Mote en un objeto MoteResponseDTO.
     * @param mote El objeto Mote a convertir.
     * @return Un objeto MoteResponseDTO con la información correspondiente.
     */
    private MoteResponseDTO convertirADTO(Mote mote) {
        Integer votos = votoRepository.countByMoteId(mote.getId());
        return new MoteResponseDTO(
                mote.getId(),
                mote.getPersonaId(),
                mote.getPersonaNombre(),
                mote.getMote(),
                mote.getDescripcion(),
                mote.getImagen(),
                votos != null ? votos : 0,
                mote.getCreatedBy(),
                mote.getCreatedAt()
        );
    }
}
