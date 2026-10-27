package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.web.OpenApiConfig;
import com.epam.gymcrm.web.dto.RegisterTrainerRequest;
import com.epam.gymcrm.web.dto.RegistrationResponse;
import com.epam.gymcrm.web.dto.TrainerProfileResponse;
import com.epam.gymcrm.web.dto.TrainerTrainingResponse;
import com.epam.gymcrm.web.dto.UpdateTrainerProfileRequest;
import com.epam.gymcrm.web.mapper.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Trainer profiles and trainings")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final ResponseMapper mapper;

    public TrainerController(TrainerService trainerService, TrainingService trainingService, ResponseMapper mapper) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Register a trainer",
            description = "Not idempotent: every call creates a new profile. The username and password are generated.")
    @ApiResponse(responseCode = "201", description = "Trainer created; the generated credentials are returned")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "The specialization does not exist")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterTrainerRequest request) {
        Trainer created = trainerService.create(new CreateTrainerRequest(
                request.firstName(), request.lastName(), request.specializationId()));
        RegistrationResponse body = mapper.toRegistration(created.getUser());
        return ResponseEntity.created(URI.create("/api/trainers/" + body.username())).body(body);
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Get a trainer profile", description = "Safe and idempotent. Callers can only read their own profile.")
    @ApiResponse(responseCode = "200", description = "The profile with the assigned trainees")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public TrainerProfileResponse getProfile(@PathVariable String username, Credentials credentials) {
        return mapper.toTrainerProfile(trainerService.getProfile(credentials.requireOwner(username)));
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Update a trainer profile",
            description = "Idempotent. The username and the specialization cannot be changed.")
    @ApiResponse(responseCode = "200", description = "The updated profile")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public TrainerProfileResponse update(@PathVariable String username,
                                         @Valid @RequestBody UpdateTrainerProfileRequest request,
                                         Credentials credentials) {
        return mapper.toTrainerProfile(trainerService.update(credentials.requireOwner(username),
                new UpdateTrainerRequest(request.firstName(), request.lastName(), request.isActive())));
    }

    @GetMapping("/{username}/trainings")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List the trainings of a trainer", description = "Safe and idempotent. All filters are optional.")
    @ApiResponse(responseCode = "200", description = "The trainings, ordered by date")
    @ApiResponse(responseCode = "400", description = "Invalid filter, for example periodFrom after periodTo")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The profile belongs to another user")
    public List<TrainerTrainingResponse> getTrainings(
            @PathVariable String username,
            @Parameter(description = "First day of the period, inclusive") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Last day of the period, inclusive") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @Parameter(description = "First or last name of the trainee") @RequestParam(required = false) String traineeName,
            Credentials credentials) {
        var criteria = new TrainerTrainingCriteria(periodFrom, periodTo, traineeName);
        return trainingService.getTrainerTrainings(credentials.requireOwner(username), criteria).stream()
                .map(mapper::toTrainerTraining)
                .toList();
    }
}
