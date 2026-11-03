package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTraineeDao implements TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee save(Trainee trainee) {
        entityManager.persist(trainee);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        return entityManager.merge(trainee);
    }

    @Override
    public void delete(Trainee trainee) {
        // cascades to the user and the trainings; trainee_trainer rows go with the owning side
        entityManager.remove(entityManager.contains(trainee) ? trainee : entityManager.merge(trainee));
    }

    /** Loads the assigned trainers together with the trainee, so callers can read them after the transaction. */
    @Override
    public Optional<Trainee> findByUsername(String username) {
        return entityManager
                .createQuery("select t from Trainee t left join fetch t.trainers where t.user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Trainee> findAllByTrainer(String trainerUsername) {
        return entityManager
                .createQuery("""
                        select te from Trainee te join te.trainers tr
                        where tr.user.username = :username
                        order by te.user.username
                        """, Trainee.class)
                .setParameter("username", trainerUsername)
                .getResultList();
    }
}
