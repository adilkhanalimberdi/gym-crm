package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaTrainingTypeDao implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findByName(String name) {
        return entityManager
                .createQuery("select t from TrainingType t where t.name = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }
}
