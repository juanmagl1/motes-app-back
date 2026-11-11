package com.example.motes.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotoId implements Serializable {
    private String usuarioId;
    private String moteId;
}
