package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Trainings are searched with the Criteria API because every filter is optional. */
@Repository
public class JpaTrainingDao implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Training save(Training training) {
        entityManager.persist(training);
        return training;
    }

    @Override
    public List<Training> findByTrainee(String traineeUsername, TraineeTrainingCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);
        Join<Training, Trainer> trainer = training.join("trainer");
        Join<Trainer, User> trainerUser = trainer.join("user");
        Join<Training, Trainee> trainee = training.join("trainee");

        List<Predicate> filters = new ArrayList<>();
        filters.add(cb.equal(trainee.join("user").<String>get("username"), traineeUsername));
        addDateRange(filters, cb, training, criteria.fromDate(), criteria.toDate());
        if (hasText(criteria.trainerName())) {
            filters.add(nameMatches(cb, trainerUser, criteria.trainerName()));
        }
        if (hasText(criteria.trainingTypeName())) {
            filters.add(cb.equal(cb.lower(training.join("trainingType").<String>get("name")),
                    criteria.trainingTypeName().trim().toLowerCase()));
        }
        return execute(query, training, filters, cb);
    }

    @Override
    public List<Training> findByTrainer(String trainerUsername, TrainerTrainingCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);
        Join<Training, Trainer> trainer = training.join("trainer");
        Join<Training, Trainee> trainee = training.join("trainee");
        Join<Trainee, User> traineeUser = trainee.join("user");

        List<Predicate> filters = new ArrayList<>();
        filters.add(cb.equal(trainer.join("user").<String>get("username"), trainerUsername));
        addDateRange(filters, cb, training, criteria.fromDate(), criteria.toDate());
        if (hasText(criteria.traineeName())) {
            filters.add(nameMatches(cb, traineeUser, criteria.traineeName()));
        }
        return execute(query, training, filters, cb);
    }

    private List<Training> execute(CriteriaQuery<Training> query, Root<Training> training,
                                   List<Predicate> filters, CriteriaBuilder cb) {
        query.select(training)
                .where(filters.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("trainingDate")), cb.asc(training.get("id")));
        return entityManager.createQuery(query).getResultList();
    }

    private void addDateRange(List<Predicate> filters, CriteriaBuilder cb, Root<Training> training,
                              LocalDate from, LocalDate to) {
        if (from != null) {
            filters.add(cb.greaterThanOrEqualTo(training.<LocalDate>get("trainingDate"), from));
        }
        if (to != null) {
            filters.add(cb.lessThanOrEqualTo(training.<LocalDate>get("trainingDate"), to));
        }
    }

    /** A "name" matches the user's first or last name, ignoring case. */
    private Predicate nameMatches(CriteriaBuilder cb, Join<?, User> user, String name) {
        String lowerName = name.trim().toLowerCase();
        return cb.or(
                cb.equal(cb.lower(user.<String>get("firstName")), lowerName),
                cb.equal(cb.lower(user.<String>get("lastName")), lowerName));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
