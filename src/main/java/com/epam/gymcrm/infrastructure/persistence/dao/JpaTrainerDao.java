package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.PageQuery;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.EntityMapper;
import com.epam.gymcrm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaTrainerDao implements TrainerDao {

    /** Fetches in one query everything a domain trainer is built from. */
    private static final String SELECT_TRAINER = """
            select t from TrainerEntity t
            join fetch t.user u
            join fetch t.specialization
            """;

    private final EntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaTrainerDao(EntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Trainer save(Trainer trainer) {
        TrainerEntity entity = new TrainerEntity(mapper.toEntity(trainer.getUser()), specializationOf(trainer));
        entityManager.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Trainer update(Trainer trainer) {
        TrainerEntity entity = entityManager.find(TrainerEntity.class, trainer.getId());
        if (entity == null) {
            throw new ResourceNotFoundException("Trainer", trainer.getUser().getUsername());
        }
        mapper.copyChanges(trainer.getUser(), entity.getUser());
        entity.setSpecialization(specializationOf(trainer));
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return entityManager
                .createQuery(SELECT_TRAINER + "where u.username = :username", TrainerEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<Trainer> findAllByUsernames(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return List.of();
        }
        return entityManager
                .createQuery(SELECT_TRAINER + "where u.username in :usernames", TrainerEntity.class)
                .setParameter("usernames", usernames)
                .getResultStream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername, PageQuery page) {
        return entityManager
                .createQuery(SELECT_TRAINER + """
                        where t not in (select assigned from TraineeEntity te join te.trainers assigned
                                        where te.user.username = :username)
                        order by u.username
                        """, TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .setFirstResult(page.offset())
                .setMaxResults(page.size())
                .getResultStream()
                .map(mapper::toDomain)
                .toList();
    }

    private TrainingTypeEntity specializationOf(Trainer trainer) {
        return entityManager.getReference(TrainingTypeEntity.class, trainer.getSpecialization().getId());
    }
}
