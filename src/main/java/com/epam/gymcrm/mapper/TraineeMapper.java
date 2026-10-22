package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    /** Builds a new trainee and its user; the generated username and password come in as credentials. */
    public Trainee toEntity(CreateTraineeRequest request, Credentials generated) {
        User user = new User(
                request.firstName().trim(),
                request.lastName().trim(),
                generated.username(),
                generated.password()
        );
        return new Trainee(user, request.dateOfBirth(), request.address());
    }
}
