package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.EntityMapper;
import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaTrainingTypeDao implements TrainingTypeDao {

    private final EntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaTrainingTypeDao(EntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        return entityManager
                .createQuery("select t from TrainingTypeEntity t where t.name = :name", TrainingTypeEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain);
    }
}
