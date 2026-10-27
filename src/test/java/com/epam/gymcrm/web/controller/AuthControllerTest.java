package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private UserService userService;

    @Test
    void login_isOk_whenCredentialsMatch() throws Exception {
        mvc.perform(get("/api/auth/login").param("username", "John.Smith").param("password", "secret"))
                .andExpect(status().isOk());

        verify(authenticationService).authenticate(new Credentials("John.Smith", "secret"));
    }

    @Test
    void login_isUnauthorized_whenCredentialsDoNotMatch() throws Exception {
        when(authenticationService.authenticate(any())).thenThrow(new AuthenticationFailedException());

        mvc.perform(get("/api/auth/login").param("username", "John.Smith").param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void login_requiresUsernameAndPassword() throws Exception {
        mvc.perform(get("/api/auth/login").param("username", "John.Smith"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'password' is required"));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void changePassword_passesOldCredentialsAndNewPassword() throws Exception {
        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John.Smith\",\"oldPassword\":\"secret\",\"newPassword\":\"fresh\"}"))
                .andExpect(status().isOk());

        verify(userService).changePassword(new Credentials("John.Smith", "secret"), "fresh");
    }

    @Test
    void changePassword_requiresAllFields() throws Exception {
        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John.Smith\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("oldPassword")))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("newPassword")));

        verifyNoInteractions(userService);
    }

    @Test
    void changePassword_isUnauthorized_whenOldPasswordIsWrong() throws Exception {
        org.mockito.Mockito.doThrow(new AuthenticationFailedException()).when(userService).changePassword(any(), any());

        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John.Smith\",\"oldPassword\":\"wrong\",\"newPassword\":\"fresh\"}"))
                .andExpect(status().isUnauthorized());
    }
}
