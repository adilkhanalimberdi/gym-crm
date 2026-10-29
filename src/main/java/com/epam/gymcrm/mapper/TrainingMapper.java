package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTrainingRequest;
import com.epam.gymcrm.model.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

	public Training toEntity(CreateTrainingRequest request) {
		return new Training(
				null,
				request.traineeId(),
				request.trainerId(),
				request.name(),
				request.type(),
				request.date(),
				request.durationMinutes()
		);
	}

}
