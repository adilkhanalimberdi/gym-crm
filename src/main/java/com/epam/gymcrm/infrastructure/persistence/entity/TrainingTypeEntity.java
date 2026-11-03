package com.epam.gymcrm.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Constant list of training types (filled by import.sql). It lives in its own table because many
 * trainings and trainers share the same few values, and being {@link Immutable} keeps the
 * application from changing it.
 */
@Entity
@Immutable
@Table(name = "training_types")
public class TrainingTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected TrainingTypeEntity() {
        // required by JPA
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
