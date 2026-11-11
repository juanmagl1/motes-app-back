package com.example.motes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaRequestDTO {
    private String nombre;
    private String email;
    private String password;
    private Boolean crearCuenta = false;
}
