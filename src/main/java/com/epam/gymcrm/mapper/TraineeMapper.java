package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.model.Trainee;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

	public Trainee toEntity(CreateTraineeRequest request, String username, String password) {
		return new Trainee(
				null,
				request.firstName(),
				request.lastName(),
				username,
				password,
				true,
				request.dateOfBirth(),
				request.address()
		);
	}

}
