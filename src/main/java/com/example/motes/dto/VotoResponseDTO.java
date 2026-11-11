package com.example.motes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotoResponseDTO {
    private Boolean voted;
    private Integer votos;
}
