package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.web.OpenApiConfig;
import com.epam.gymcrm.web.dto.RegisterTraineeRequest;
import com.epam.gymcrm.web.dto.RegistrationResponse;
import com.epam.gymcrm.web.dto.TraineeProfileResponse;
import com.epam.gymcrm.web.dto.TraineeTrainingResponse;
import com.epam.gymcrm.web.dto.TrainerSummary;
import com.epam.gymcrm.web.dto.UpdateTraineeProfileRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.web.mapper.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainees", description = "Trainee profiles, their trainers and trainings")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final ResponseMapper mapper;

    public TraineeController(TraineeService traineeService, TrainingService trainingService, ResponseMapper mapper) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Register a trainee",
            description = "Not idempotent: every call creates a new profile. The username and password are generated.")
    @ApiResponse(responseCode = "201", description = "Trainee created; the generated credentials are returned")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterTraineeRequest request) {
        Trainee created = traineeService.create(new CreateTraineeRequest(
                request.firstName(), request.lastName(), request.dateOfBirth(), request.address()));
        RegistrationResponse body = mapper.toRegistration(created.getUser());
        return ResponseEntity.created(URI.create("/api/trainees/" + body.username())).body(body);
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Get a trainee profile", description = "Safe and idempotent. Callers can only read their own profile.")
    @ApiResponse(responseCode = "200", description = "The profile with the assigned trainers")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public TraineeProfileResponse getProfile(@PathVariable String username, Credentials credentials) {
        return mapper.toTraineeProfile(traineeService.getProfile(credentials.requireOwner(username)));
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Update a trainee profile",
            description = "Idempotent. The username cannot be changed. Optional fields that are left out are cleared.")
    @ApiResponse(responseCode = "200", description = "The updated profile")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public TraineeProfileResponse update(@PathVariable String username,
                                         @Valid @RequestBody UpdateTraineeProfileRequest request,
                                         Credentials credentials) {
        Trainee updated = traineeService.update(credentials.requireOwner(username), new UpdateTraineeRequest(
                request.firstName(), request.lastName(), request.dateOfBirth(), request.address(),
                request.isActive()));
        return mapper.toTraineeProfile(updated);
    }

    @DeleteMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Delete a trainee profile",
            description = "Hard delete; the trainings of the trainee are deleted with it.")
    @ApiResponse(responseCode = "200", description = "Profile deleted")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public void delete(@PathVariable String username, Credentials credentials) {
        traineeService.delete(credentials.requireOwner(username));
    }

    @GetMapping("/{username}/unassigned-trainers")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List active trainers that are not assigned to the trainee",
            description = "Safe and idempotent.")
    @ApiResponse(responseCode = "200", description = "The trainers, ordered by username")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public List<TrainerSummary> getUnassignedTrainers(@PathVariable String username, Credentials credentials) {
        return mapper.toTrainerSummaries(traineeService.getUnassignedTrainers(credentials.requireOwner(username)));
    }

    @PutMapping("/{username}/trainers")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Replace the trainers of a trainee",
            description = "Idempotent. The trainee ends up with exactly the listed trainers.")
    @ApiResponse(responseCode = "200", description = "The new list of trainers")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    @ApiResponse(responseCode = "404", description = "One of the trainers does not exist")
    public List<TrainerSummary> updateTrainers(@PathVariable String username,
                                               @Valid @RequestBody UpdateTraineeTrainersRequest request,
                                               Credentials credentials) {
        List<String> trainerUsernames = request.trainers().stream().map(trainer -> trainer.username()).toList();
        Trainee updated = traineeService.updateTrainers(credentials.requireOwner(username), trainerUsernames);
        return mapper.toTrainerSummaries(updated.getTrainers());
    }

    @GetMapping("/{username}/trainings")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List the trainings of a trainee", description = "Safe and idempotent. All filters are optional.")
    @ApiResponse(responseCode = "200", description = "The trainings, ordered by date")
    @ApiResponse(responseCode = "400", description = "Invalid filter, for example periodFrom after periodTo")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public List<TraineeTrainingResponse> getTrainings(
            @PathVariable String username,
            @Parameter(description = "First day of the period, inclusive") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Last day of the period, inclusive") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @Parameter(description = "First or last name of the trainer") @RequestParam(required = false) String trainerName,
            @Parameter(description = "Name of the training type") @RequestParam(required = false) String trainingType,
            Credentials credentials) {
        var criteria = new TraineeTrainingCriteria(periodFrom, periodTo, trainerName, trainingType);
        return trainingService.getTraineeTrainings(credentials.requireOwner(username), criteria).stream()
                .map(mapper::toTraineeTraining)
                .toList();
    }
}
