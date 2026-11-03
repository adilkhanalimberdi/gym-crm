package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.EntityMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTraineeDao implements TraineeDao {

    private final EntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaTraineeDao(EntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Trainee save(Trainee trainee) {
        TraineeEntity entity = new TraineeEntity(
                mapper.toEntity(trainee.getUser()),
                trainee.getDateOfBirth(),
                trainee.getAddress());
        entity.replaceTrainers(loadTrainersOf(trainee));
        entityManager.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Trainee update(Trainee trainee) {
        TraineeEntity entity = findEntity(trainee);
        mapper.copyChanges(trainee.getUser(), entity.getUser());
        entity.setDateOfBirth(trainee.getDateOfBirth());
        entity.setAddress(trainee.getAddress());
        entity.replaceTrainers(loadTrainersOf(trainee));
        return mapper.toDomain(entity);
    }

    @Override
    public void delete(Trainee trainee) {
        // cascades to the user and the trainings; trainee_trainer rows go with the owning side
        entityManager.remove(findEntity(trainee));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return entityManager
                .createQuery("""
                        select t from TraineeEntity t
                        join fetch t.user u
                        left join fetch t.trainers trainer
                        left join fetch trainer.user
                        left join fetch trainer.specialization
                        where u.username = :username
                        """, TraineeEntity.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    private TraineeEntity findEntity(Trainee trainee) {
        TraineeEntity entity = entityManager.find(TraineeEntity.class, trainee.getId());
        if (entity == null) {
            throw new ResourceNotFoundException("Trainee", trainee.getUser().getUsername());
        }
        return entity;
    }

    private List<TrainerEntity> loadTrainersOf(Trainee trainee) {
        List<Long> trainerIds = trainee.getTrainers().stream().map(Trainer::getId).toList();
        if (trainerIds.isEmpty()) {
            return List.of();
        }
        return entityManager
                .createQuery("""
                        select t from TrainerEntity t
                        join fetch t.user
                        join fetch t.specialization
                        where t.id in :ids
                        """, TrainerEntity.class)
                .setParameter("ids", trainerIds)
                .getResultList();
    }
}
