package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dto.PageQuery;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.UserEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.EntityMapper;
import com.epam.gymcrm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Trainings are searched with the Criteria API because every filter is optional. Each search is a single
 * query: the joins it filters on also fetch everything a domain training is built from.
 */
@Repository
public class JpaTrainingDao implements TrainingDao {

    private final EntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaTrainingDao(EntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Training save(Training training) {
        TrainingEntity entity = new TrainingEntity(
                entityManager.getReference(TraineeEntity.class, training.getTrainee().id()),
                entityManager.getReference(TrainerEntity.class, training.getTrainer().id()),
                training.getTrainingName(),
                entityManager.getReference(TrainingTypeEntity.class, training.getTrainingType().getId()),
                training.getTrainingDate(),
                training.getDuration());
        entityManager.persist(entity);
        return training.withId(entity.getId());
    }

    @Override
    public List<Training> findByTrainee(String traineeUsername, TraineeTrainingCriteria criteria, PageQuery page) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> training = query.from(TrainingEntity.class);
        Join<TrainerEntity, UserEntity> trainerUser = fetchTrainerUser(training);
        Join<TraineeEntity, UserEntity> traineeUser = fetchTraineeUser(training);
        Join<TrainingEntity, TrainingTypeEntity> trainingType = fetchJoin(training, "trainingType");

        List<Predicate> filters = new ArrayList<>();
        filters.add(cb.equal(traineeUser.<String>get("username"), traineeUsername));
        addDateRange(filters, cb, training, criteria.fromDate(), criteria.toDate());
        if (hasText(criteria.trainerName())) {
            filters.add(nameMatches(cb, trainerUser, criteria.trainerName()));
        }
        if (hasText(criteria.trainingTypeName())) {
            filters.add(cb.equal(cb.lower(trainingType.<String>get("name")),
                    criteria.trainingTypeName().trim().toLowerCase()));
        }
        return execute(query, training, filters, cb, page);
    }

    @Override
    public List<Training> findByTrainer(String trainerUsername, TrainerTrainingCriteria criteria, PageQuery page) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> training = query.from(TrainingEntity.class);
        Join<TrainerEntity, UserEntity> trainerUser = fetchTrainerUser(training);
        Join<TraineeEntity, UserEntity> traineeUser = fetchTraineeUser(training);
        fetchJoin(training, "trainingType");

        List<Predicate> filters = new ArrayList<>();
        filters.add(cb.equal(trainerUser.<String>get("username"), trainerUsername));
        addDateRange(filters, cb, training, criteria.fromDate(), criteria.toDate());
        if (hasText(criteria.traineeName())) {
            filters.add(nameMatches(cb, traineeUser, criteria.traineeName()));
        }
        return execute(query, training, filters, cb, page);
    }

    private List<Training> execute(CriteriaQuery<TrainingEntity> query,
                                   Root<TrainingEntity> training,
                                   List<Predicate> filters,
                                   CriteriaBuilder cb,
                                   PageQuery page) {
        query.select(training)
                .where(filters.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("trainingDate")), cb.asc(training.get("id")));
        // only to-one associations are fetched, so the database can apply the limit itself
        return entityManager.createQuery(query)
                .setFirstResult(page.offset())
                .setMaxResults(page.size())
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    private Join<TrainerEntity, UserEntity> fetchTrainerUser(Root<TrainingEntity> training) {
        Join<TrainingEntity, TrainerEntity> trainer = fetchJoin(training, "trainer");
        return fetchJoin(trainer, "user");
    }

    private Join<TraineeEntity, UserEntity> fetchTraineeUser(Root<TrainingEntity> training) {
        Join<TrainingEntity, TraineeEntity> trainee = fetchJoin(training, "trainee");
        return fetchJoin(trainee, "user");
    }

    /** A Hibernate fetch is also a join, so filtering on it adds no second SQL join to the same table. */
    @SuppressWarnings("unchecked")
    private static <X, Y> Join<X, Y> fetchJoin(From<?, X> from, String attribute) {
        return (Join<X, Y>) from.<X, Y>fetch(attribute);
    }

    private void addDateRange(List<Predicate> filters,
                              CriteriaBuilder cb,
                              Root<TrainingEntity> training,
                              LocalDate from,
                              LocalDate to) {
        if (from != null) {
            filters.add(cb.greaterThanOrEqualTo(training.<LocalDate>get("trainingDate"), from));
        }
        if (to != null) {
            filters.add(cb.lessThanOrEqualTo(training.<LocalDate>get("trainingDate"), to));
        }
    }

    /** A "name" matches the user's first or last name, ignoring case. */
    private Predicate nameMatches(CriteriaBuilder cb, Join<?, UserEntity> user, String name) {
        String lowerName = name.trim().toLowerCase();
        return cb.or(
                cb.equal(cb.lower(user.<String>get("firstName")), lowerName),
                cb.equal(cb.lower(user.<String>get("lastName")), lowerName));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
