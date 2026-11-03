package com.epam.gymcrm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Setter
    private String address;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Trainers assigned to this trainee; the trainee owns the trainee_trainer join table. */
    @ManyToMany
    @JoinTable(name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private Set<Trainer> trainers = new HashSet<>();

    /** Removing a trainee removes all of its trainings as well. */
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Training> trainings = new HashSet<>();

    public Trainee(
            User user,
            LocalDate dateOfBirth,
            String address
    ) {
        this.user = user;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    /** Replaces the assigned trainers with the given ones. */
    public void replaceTrainers(Collection<Trainer> newTrainers) {
        trainers.retainAll(newTrainers);
        trainers.addAll(newTrainers);
    }

    @Override
    public String toString() {
        return "Trainee[id=" + id + ", username=" + user.getUsername() + "]";
    }
}
