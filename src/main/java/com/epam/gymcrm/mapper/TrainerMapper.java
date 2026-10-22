package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    /**
     * Builds a new trainer and its user. The specialization is looked up by the service
     * (the request only has its name); the generated username and password come in as credentials.
     */
    public Trainer toEntity(CreateTrainerRequest request, TrainingType specialization, Credentials generated) {
        User user = new User(
                request.firstName().trim(),
                request.lastName().trim(),
                generated.username(),
                generated.password()
        );
        return new Trainer(user, specialization);
    }
}
