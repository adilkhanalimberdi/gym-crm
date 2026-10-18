package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.config.StorageConfig;
import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TraineeDaoImpl extends AbstractInMemoryDao<Trainee> implements TraineeDao {

    @Autowired
    public void setStorage(@Qualifier(StorageConfig.TRAINEE_STORAGE) Map<Long, Trainee> storage) {
        useStorage(storage);
    }

    @Override
    public boolean update(Trainee trainee) {
        return replace(trainee.id(), trainee);
    }

    @Override
    public boolean deleteById(Long id) {
        return remove(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return anyMatch(t -> t.username().equals(username));
    }

    @Override
    protected Trainee withId(Trainee entity, Long id) {
        return entity.withId(id);
    }

    @Override
    protected String entityName() {
        return "trainee";
    }
}
