package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.config.StorageConfig;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainingDaoImpl extends AbstractInMemoryDao<Training> implements TrainingDao {

    @Autowired
    public void setStorage(@Qualifier(StorageConfig.TRAINING_STORAGE) Map<Long, Training> storage) {
        useStorage(storage);
    }

    @Override
    protected Training withId(Training entity, Long id) {
        return entity.withId(id);
    }

    @Override
    protected String entityName() {
        return "training";
    }
}
