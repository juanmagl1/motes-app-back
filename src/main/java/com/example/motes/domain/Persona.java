package com.example.motes.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "persona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String nombre;

    private String email;

    @Column(columnDefinition = "VARCHAR(36)")
    private String userId; // FK to Usuario if account was created

    @Column(nullable = false, columnDefinition = "VARCHAR(36)")
    private String createdBy; // FK to Usuario who created this persona

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
