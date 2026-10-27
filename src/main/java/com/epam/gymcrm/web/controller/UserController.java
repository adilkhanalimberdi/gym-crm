package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.service.UserService;
import com.epam.gymcrm.web.OpenApiConfig;
import com.epam.gymcrm.web.dto.ActivationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Account operations shared by trainees and trainers")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{username}/activation")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
    @Operation(summary = "Activate or deactivate a trainee or a trainer",
            description = "Not idempotent: asking for the state the user is already in is a conflict.")
    @ApiResponse(responseCode = "200", description = "State changed")
    @ApiResponse(responseCode = "400", description = "isActive is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")
    @ApiResponse(responseCode = "403", description = "The account belongs to another user")
    @ApiResponse(responseCode = "409", description = "The user is already in the requested state")
    public void changeActivation(@PathVariable String username,
                                 @Valid @RequestBody ActivationRequest request,
                                 Credentials credentials) {
        userService.changeActivation(credentials.requireOwner(username), request.isActive());
    }
}
