package com.example.motes.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "voto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"usuario_id", "mote_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VotoId.class)
public class Voto {

    @Id
    @Column(name = "usuario_id", columnDefinition = "VARCHAR(36)")
    private String usuarioId;

    @Id
    @Column(name = "mote_id", columnDefinition = "VARCHAR(36)")
    private String moteId;
}


