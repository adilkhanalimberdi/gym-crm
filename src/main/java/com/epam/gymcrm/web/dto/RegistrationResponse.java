package com.epam.gymcrm.web.dto;

/** The generated credentials of a new profile; this is the only time the password is returned. */
public record RegistrationResponse(String username, String password) {
}
