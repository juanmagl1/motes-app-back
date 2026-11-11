package com.example.motes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponseDTO {
    private String id;
    private String nombre;
    private String email;
    private String userId;
    private String createdBy;
    private LocalDateTime createdAt;
}
