package com.example.motes.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean isAdmin = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        if (isAdmin == null) {
            isAdmin = false;
        }
    }
}
