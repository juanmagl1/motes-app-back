package com.example.motes.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mote {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, columnDefinition = "VARCHAR(36)")
    private String personaId; // FK to Persona

    @Column(nullable = false)
    private String personaNombre; // Denormalized for performance

    @Column(nullable = false)
    private String mote;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String imagen; // Base64 encoded image

    @Column(nullable = false, columnDefinition = "VARCHAR(36)")
    private String createdBy; // FK to Usuario who created this mote

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }
}
