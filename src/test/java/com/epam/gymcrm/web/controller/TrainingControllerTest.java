package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.exception.AccessDeniedException;
import com.epam.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.epam.gymcrm.web.support.TestData.ANNA;
import static com.epam.gymcrm.web.support.TestData.JOHN;
import static com.epam.gymcrm.web.support.TestData.basic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
class TrainingControllerTest {

    private static final String AUTH = basic(JOHN, "secret");
    private static final String VALID_BODY = """
            {"traineeUsername":"John.Smith","trainerUsername":"Anna.Lee","trainingName":"Morning Yoga",
             "trainingDate":"2026-11-02","duration":60}
            """;

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TrainingService trainingService;

    @Test
    void addTraining_passesRequestToService() throws Exception {
        mvc.perform(post("/api/trainings").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk());

        verify(trainingService).addTraining(new Credentials(JOHN, "secret"),
                new NewTraining(JOHN, ANNA, "Morning Yoga", LocalDate.of(2026, 11, 2), 60));
    }

    @Test
    void addTraining_requiresCredentials() throws Exception {
        mvc.perform(post("/api/trainings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trainingService);
    }

    @Test
    void addTraining_rejectsMissingAndInvalidFields() throws Exception {
        mvc.perform(post("/api/trainings").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("trainingDate")));
        mvc.perform(post("/api/trainings").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"duration\":60", "\"duration\":0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("duration")));

        verifyNoInteractions(trainingService);
    }

    @Test
    void addTraining_isForbidden_forNonParticipants() throws Exception {
        doThrow(new AccessDeniedException("Only the trainee or the trainer can add this training"))
                .when(trainingService).addTraining(any(), any());

        mvc.perform(post("/api/trainings").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
    }
}
