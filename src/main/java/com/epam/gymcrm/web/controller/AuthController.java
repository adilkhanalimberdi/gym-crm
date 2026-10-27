package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.UserService;
import com.epam.gymcrm.web.dto.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and password change")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @GetMapping("/login")
    @Operation(summary = "Check a username and password",
            description = "Safe and idempotent. Answers 200 when the pair matches a user.")
    @ApiResponse(responseCode = "200", description = "Username and password match")
    @ApiResponse(responseCode = "400", description = "Username or password is missing")
    @ApiResponse(responseCode = "401", description = "Username and password do not match")
    public void login(@RequestParam String username, @RequestParam String password) {
        authenticationService.authenticate(new Credentials(username, password));
    }

    @PutMapping("/password")
    @Operation(summary = "Change the password",
            description = "Idempotent. The old password has to match the current one.")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Username and old password do not match")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(new Credentials(request.username(), request.oldPassword()), request.newPassword());
    }
}
