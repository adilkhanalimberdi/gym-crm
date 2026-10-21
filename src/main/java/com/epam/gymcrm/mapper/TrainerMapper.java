package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.model.Trainer;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

	public Trainer toEntity(CreateTrainerRequest request, String username, String password) {
		return new Trainer(
				null,
				request.firstName(),
				request.lastName(),
				username,
				password,
				true,
				request.specialization()
		);
	}

}
