package com.epam.gymcrm.infrastructure.persistence.mapper;

import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.UserEntity;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingParticipant;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts between JPA entities and domain objects. Reading an association loads it if it is still lazy,
 * so the DAOs fetch in their queries everything the conversion below reads.
 */
@Component
public class EntityMapper {

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getUsername(),
                entity.getPassword(),
                entity.isActive());
    }

    public TrainingType toDomain(TrainingTypeEntity entity) {
        return new TrainingType(entity.getId(), entity.getName());
    }

    public Trainer toDomain(TrainerEntity entity) {
        return new Trainer(entity.getId(), toDomain(entity.getUser()), toDomain(entity.getSpecialization()));
    }

    public Trainee toDomain(TraineeEntity entity) {
        List<Trainer> trainers = entity.getTrainers().stream().map(this::toDomain).toList();
        return new Trainee(
                entity.getId(),
                toDomain(entity.getUser()),
                entity.getDateOfBirth(),
                entity.getAddress(),
                trainers);
    }

    public Training toDomain(TrainingEntity entity) {
        return new Training(
                entity.getId(),
                toParticipant(entity.getTrainee().getId(), entity.getTrainee().getUser()),
                toParticipant(entity.getTrainer().getId(), entity.getTrainer().getUser()),
                entity.getTrainingName(),
                toDomain(entity.getTrainingType()),
                entity.getTrainingDate(),
                entity.getDuration());
    }

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                user.isActive());
    }

    /** Copies everything that can change; the username and the id stay as they are. */
    public void copyChanges(User user, UserEntity entity) {
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setPassword(user.getPassword());
        entity.setActive(user.isActive());
    }

    private TrainingParticipant toParticipant(Long id, UserEntity user) {
        return new TrainingParticipant(id, user.getUsername(), user.getFirstName(), user.getLastName());
    }
}
