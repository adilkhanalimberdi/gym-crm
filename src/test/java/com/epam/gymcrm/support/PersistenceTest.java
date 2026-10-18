package com.epam.gymcrm.support;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
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

    protected TrainingType trainingType(String name) {
        return entityManager.createQuery("select t from TrainingType t where t.name = :name", TrainingType.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    protected Trainee persistTrainee(String firstName, String lastName) {
        User user = new User(firstName, lastName, firstName + "." + lastName, "secret");
        Trainee trainee = new Trainee(user, LocalDate.of(1990, 1, 1), "Main Street");
        entityManager.persist(trainee);
        return trainee;
    }

    protected Trainer persistTrainer(String firstName, String lastName, String specialization) {
        User user = new User(firstName, lastName, firstName + "." + lastName, "secret");
        Trainer trainer = new Trainer(user, trainingType(specialization));
        entityManager.persist(trainer);
        return trainer;
    }

    protected Training persistTraining(Trainee trainee, Trainer trainer, String name, String type, LocalDate date) {
        Training training = new Training(trainee, trainer, name, trainingType(type), date, 60);
        entityManager.persist(training);
        return training;
    }
}
