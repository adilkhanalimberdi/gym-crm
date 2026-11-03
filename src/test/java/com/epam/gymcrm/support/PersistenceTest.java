package com.epam.gymcrm.support;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Base for tests that run against the real Spring context and the H2 database.
 * Every test runs in its own transaction that is rolled back, so tests do not affect each other.
 */
@SpringJUnitConfig(AppConfig.class)
@Transactional
public abstract class PersistenceTest {

    @PersistenceContext
    protected EntityManager entityManager;

    /** Sends pending changes to the database and empties the session, so later reads hit the database. */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected long count(String jpql) {
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    protected TrainingTypeEntity trainingType(String name) {
        return entityManager.createQuery("select t from TrainingTypeEntity t where t.name = :name", TrainingTypeEntity.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    protected TraineeEntity persistTrainee(String firstName, String lastName) {
        TraineeEntity trainee = new TraineeEntity(newUser(firstName, lastName), LocalDate.of(1990, 1, 1), "Main Street");
        entityManager.persist(trainee);
        return trainee;
    }

    protected TrainerEntity persistTrainer(String firstName, String lastName, String specialization) {
        TrainerEntity trainer = new TrainerEntity(newUser(firstName, lastName), trainingType(specialization));
        entityManager.persist(trainer);
        return trainer;
    }

    protected TrainingEntity persistTraining(TraineeEntity trainee,
                                             TrainerEntity trainer,
                                             String name,
                                             String type,
                                             LocalDate date) {
        TrainingEntity training = new TrainingEntity(trainee, trainer, name, trainingType(type), date, 60);
        entityManager.persist(training);
        return training;
    }

    private UserEntity newUser(String firstName, String lastName) {
        return new UserEntity(firstName, lastName, firstName + "." + lastName, "secret", true);
    }
}
