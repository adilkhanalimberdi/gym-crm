package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTrainingTypeDao implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager
                .createQuery("select t from TrainingType t order by t.id", TrainingType.class)
                .getResultList();
    }
}
