package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.web.OpenApiConfig;
import com.epam.gymcrm.web.dto.AddTrainingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Add a training",
            description = "Not idempotent: every call adds a training. Only the trainee or the trainer of the training "
                    + "can add it. Its type is the specialization of the trainer. Trainings cannot be changed or deleted.")
    @ApiResponse(responseCode = "200", description = "Training added")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The caller is neither the trainee nor the trainer")
    @ApiResponse(responseCode = "404", description = "The trainee or the trainer does not exist")
    public void addTraining(@Valid @RequestBody AddTrainingRequest request, Credentials credentials) {
        trainingService.addTraining(credentials, new NewTraining(request.traineeUsername(), request.trainerUsername(),
                request.trainingName(), request.trainingDate(), request.duration()));
    }
}
