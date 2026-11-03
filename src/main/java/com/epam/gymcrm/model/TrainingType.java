package com.epam.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Constant list of training types (filled by import.sql). It lives in its own table because many
 * trainings and trainers share the same few values, and being {@link Immutable} keeps the
 * application from changing it.
 */
@Entity
@Immutable
@Table(name = "training_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Override
    public String toString() {
        return "TrainingType[id=" + id + ", name=" + name + "]";
    }
}
