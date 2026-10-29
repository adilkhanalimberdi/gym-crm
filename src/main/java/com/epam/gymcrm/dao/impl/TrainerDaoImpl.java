package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.storage.InMemoryStorageConfig;
import com.epam.gymcrm.storage.UsernameIndex;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainerDaoImpl extends AbstractInMemoryDao<Trainer> implements TrainerDao {

    private UsernameIndex usernameIndex;

    @Autowired
    public void setStorage(@Qualifier(InMemoryStorageConfig.TRAINER_STORAGE) Map<Long, Trainer> storage) {
        useStorage(storage);
    }

    @Autowired
    public void setUsernameIndex(UsernameIndex usernameIndex) {
        this.usernameIndex = usernameIndex;
    }

    @Override
    public Trainer create(Trainer trainer) {
        return usernameIndex.insertIfFree(trainer.username(), () -> super.create(trainer));
    }

    @Override
    public boolean update(Trainer trainer) {
        return replace(trainer.id(), trainer);
    }

    @Override
    public boolean existsByUsername(String username) {
        return anyMatch(t -> t.username().equals(username));
    }

    @Override
    protected Trainer withId(Trainer entity, Long id) {
        return entity.withId(id);
    }

    @Override
    protected String entityName() {
        return "trainer";
    }
}
