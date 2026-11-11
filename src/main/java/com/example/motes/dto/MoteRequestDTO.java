package com.example.motes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoteRequestDTO {
    private String personaId;
    private String mote;
    private String descripcion;
    private String imagen; // Base64 encoded image (optional)
}
