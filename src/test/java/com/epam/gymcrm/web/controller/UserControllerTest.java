package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.ConflictException;
import com.epam.gymcrm.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epam.gymcrm.web.support.TestData.JOHN;
import static com.epam.gymcrm.web.support.TestData.basic;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String AUTH = basic(JOHN, "secret");

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private UserService userService;

    @Test
    void changeActivation_passesRequestedState() throws Exception {
        mvc.perform(patch("/api/users/" + JOHN + "/activation").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isActive\":false}"))
                .andExpect(status().isOk());

        verify(userService).changeActivation(new Credentials(JOHN, "secret"), false);
    }

    @Test
    void changeActivation_isConflict_whenUserIsAlreadyInThatState() throws Exception {
        doThrow(new ConflictException("User 'John.Smith' is already active"))
                .when(userService).changeActivation(any(), anyBoolean());

        mvc.perform(patch("/api/users/" + JOHN + "/activation").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isActive\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User 'John.Smith' is already active"));
    }

    @Test
    void changeActivation_requiresIsActive() throws Exception {
        mvc.perform(patch("/api/users/" + JOHN + "/activation").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void changeActivation_isForbidden_forSomeoneElsesAccount() throws Exception {
        mvc.perform(patch("/api/users/Kate.Brown/activation").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isActive\":false}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
