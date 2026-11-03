package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.ConflictException;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Account operations shared by trainees and trainers: password and activation. */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AuthenticationService authenticationService;

    public UserService(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    public void changePassword(Credentials credentials, String newPassword) {
        User user = authenticationService.authenticate(credentials);
        user.setPassword(newPassword);
        log.info("Changed password of user '{}'", credentials.username());
    }

    /** Not idempotent: asking for the state the user is already in is a conflict. */
    public void changeActivation(Credentials credentials, boolean active) {
        User user = authenticationService.authenticate(credentials);
        String state = active ? "active" : "inactive";
        if (user.isActive() == active) {
            log.warn("User '{}' is already {}", credentials.username(), state);
            throw new ConflictException("User '" + credentials.username() + "' is already " + state);
        }
        user.setActive(active);
        log.info("User '{}' is now {}", credentials.username(), state);
    }
}
