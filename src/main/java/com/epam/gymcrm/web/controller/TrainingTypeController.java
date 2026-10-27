package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.service.TrainingTypeService;
import com.epam.gymcrm.web.OpenApiConfig;
import com.epam.gymcrm.web.dto.TrainingTypeResponse;
import com.epam.gymcrm.web.mapper.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final ResponseMapper mapper;

    public TrainingTypeController(TrainingTypeService trainingTypeService, ResponseMapper mapper) {
        this.trainingTypeService = trainingTypeService;
        this.mapper = mapper;
    }

    @GetMapping
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "List the training types",
            description = "Safe and idempotent. The list is constant and cannot be changed through the API.")
    @ApiResponse(responseCode = "200", description = "All training types")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    public List<TrainingTypeResponse> getTrainingTypes(Credentials credentials) {
        return mapper.toTrainingTypes(trainingTypeService.findAll(credentials));
    }
}
