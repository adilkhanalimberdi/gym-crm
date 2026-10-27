package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String username,
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {

    /** The password is deliberately left out so it can never leak into logs. */
    @Override
    public String toString() {
        return "ChangePasswordRequest[username=" + username + "]";
    }
}
