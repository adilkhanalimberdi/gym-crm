package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.request.RegisterTraineeRequest;
import com.epam.gymcrm.dto.request.TrainerUsername;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.response.RegistrationResponse;
import com.epam.gymcrm.dto.response.TraineeProfileResponse;
import com.epam.gymcrm.dto.response.TraineeTrainingResponse;
import com.epam.gymcrm.dto.response.TrainerSummary;
import com.epam.gymcrm.mapper.GymCrmMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.web.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Tag(name = "Trainees", description = "Trainee profiles, their trainers and trainings")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final GymCrmMapper mapper;

    @PostMapping
    @Operation(summary = "Register a trainee",
            description = "Not idempotent: every call creates a new profile. The username and password are generated.")
    @ApiResponse(responseCode = "201", description = "Trainee created; the generated credentials are returned")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterTraineeRequest request) {
        Trainee created = traineeService.create(request);
        RegistrationResponse body = mapper.toRegistration(created.getUser());
        return ResponseEntity.created(URI.create("/api/trainees/" + body.username()))
                .body(body);
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Get a trainee profile",
            description = "Safe and idempotent. Callers can only read their own profile.")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username,
                                                             Credentials credentials) {
        Trainee trainee = traineeService.getProfile(credentials.requireOwner(username));
        return ResponseEntity.ok(mapper.toTraineeProfileResponse(trainee));
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Update a trainee profile",
            description = "Idempotent. The username cannot be changed. Optional fields that are left out are cleared.")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public ResponseEntity<TraineeProfileResponse> update(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request,
            Credentials credentials
    ) {
        Trainee updated = traineeService.update(credentials.requireOwner(username), request);
        return ResponseEntity.ok(mapper.toTraineeProfileResponse(updated));
    }

    @DeleteMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Delete a trainee profile",
            description = "Hard delete; the trainings of the trainee are deleted with it.")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public ResponseEntity<Void> delete(@PathVariable String username, Credentials credentials) {
        traineeService.delete(credentials.requireOwner(username));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List active trainers that are not assigned to the trainee",
            description = "Safe and idempotent. The trainers are ordered by username.")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public ResponseEntity<List<TrainerSummary>> getUnassignedTrainers(@PathVariable String username,
                                                                      Credentials credentials) {
        var trainers = traineeService.getUnassignedTrainers(credentials.requireOwner(username));
        return ResponseEntity.ok(mapper.toTrainerSummaries(trainers));
    }

    @PutMapping("/{username}/trainers")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Replace the trainers of a trainee",
            description = "Idempotent. The trainee ends up with exactly the listed trainers.")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    @ApiResponse(responseCode = "404", description = "One of the trainers does not exist")
    public ResponseEntity<List<TrainerSummary>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            Credentials credentials
    ) {
        List<String> trainerUsernames = request.trainers().stream()
                .map(TrainerUsername::username)
                .toList();
        Trainee updated = traineeService.updateTrainers(credentials.requireOwner(username), trainerUsernames);
        return ResponseEntity.ok(mapper.toTrainerSummaries(updated.getTrainers()));
    }

    @GetMapping("/{username}/trainings")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List the trainings of a trainee",
            description = "Safe and idempotent. All filters are optional. The trainings are ordered by date.")
    @ApiResponse(responseCode = "400", description = "Invalid filter, for example periodFrom after periodTo")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @PathVariable String username,
            @Parameter(description = "First day of the period, inclusive")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Last day of the period, inclusive")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @Parameter(description = "First or last name of the trainer")
            @RequestParam(required = false) String trainerName,
            @Parameter(description = "Name of the training type")
            @RequestParam(required = false) String trainingType,
            Credentials credentials
    ) {
        var criteria = new TraineeTrainingCriteria(
                periodFrom,
                periodTo,
                trainerName,
                trainingType
        );
        List<TraineeTrainingResponse> trainings = trainingService
                .getTraineeTrainings(credentials.requireOwner(username), criteria)
                .stream()
                .map(mapper::toTraineeTrainingResponse)
                .toList();
        return ResponseEntity.ok(trainings);
    }
}
