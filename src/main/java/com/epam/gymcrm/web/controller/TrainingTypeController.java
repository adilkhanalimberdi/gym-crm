package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.response.TrainingTypeResponse;
import com.epam.gymcrm.mapper.GymCrmMapper;
import com.epam.gymcrm.service.TrainingTypeService;
import com.epam.gymcrm.web.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@RequiredArgsConstructor
@Tag(name = "Training types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final GymCrmMapper mapper;

    @GetMapping
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List the training types",
            description = "Safe and idempotent. The list is constant and cannot be changed through the API.")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes(Credentials credentials) {
        var trainingTypes = trainingTypeService.findAll(credentials);
        return ResponseEntity.ok(mapper.toTrainingTypeResponses(trainingTypes));
    }
}
