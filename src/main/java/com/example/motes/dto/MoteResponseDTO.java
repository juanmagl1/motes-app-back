package com.example.motes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoteResponseDTO {
    private String id;
    private String personaId;
    private String personaNombre;
    private String mote;
    private String descripcion;
    private String imagen;
    private Integer votos;
    private String createdBy;
    private LocalDateTime createdAt;
}
