package com.epam.gymcrm.dto.request;

import java.time.LocalDate;

public record CreateTraineeRequest(
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		String address
) {}
