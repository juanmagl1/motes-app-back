package com.example.motes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private String id;
    private String email;
    private String nombre;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
}
