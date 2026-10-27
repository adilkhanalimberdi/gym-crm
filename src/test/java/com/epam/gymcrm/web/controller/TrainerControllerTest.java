package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TrainerProfile;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.web.mapper.ResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.epam.gymcrm.web.support.TestData.ANNA;
import static com.epam.gymcrm.web.support.TestData.JOHN;
import static com.epam.gymcrm.web.support.TestData.basic;
import static com.epam.gymcrm.web.support.TestData.trainee;
import static com.epam.gymcrm.web.support.TestData.trainer;
import static com.epam.gymcrm.web.support.TestData.training;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@Import(ResponseMapper.class)
class TrainerControllerTest {

    private static final Credentials CREDENTIALS = new Credentials(ANNA, "pw");
    private static final String AUTH = basic(ANNA, "pw");

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TrainerService trainerService;
    @MockitoBean
    private TrainingService trainingService;

    @Test
    void register_returnsGeneratedCredentials() throws Exception {
        when(trainerService.create(any(CreateTrainerRequest.class))).thenReturn(trainer());

        mvc.perform(post("/api/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Lee\",\"specializationId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trainers/Anna.Lee"))
                .andExpect(jsonPath("$.username").value(ANNA))
                .andExpect(jsonPath("$.password").value("pw"));

        verify(trainerService).create(new CreateTrainerRequest("Anna", "Lee", 2L));
    }

    @Test
    void register_requiresSpecialization() throws Exception {
        mvc.perform(post("/api/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Lee\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("specializationId")));

        verifyNoInteractions(trainerService);
    }

    @Test
    void register_isNotFound_forUnknownSpecialization() throws Exception {
        when(trainerService.create(any())).thenThrow(new EntityNotFoundException("Training type", "99"));

        mvc.perform(post("/api/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Lee\",\"specializationId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_returnsProfileWithTrainees() throws Exception {
        when(trainerService.getProfile(CREDENTIALS)).thenReturn(new TrainerProfile(trainer(), List.of(trainee())));

        mvc.perform(get("/api/trainers/" + ANNA).header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(ANNA))
                .andExpect(jsonPath("$.specialization.name").value("Yoga"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainees[0].username").value(JOHN))
                .andExpect(jsonPath("$.trainees[0].lastName").value("Smith"));
    }

    @Test
    void getProfile_isForbidden_forSomeoneElsesProfile() throws Exception {
        mvc.perform(get("/api/trainers/Mark.Taylor").header("Authorization", AUTH))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    @Test
    void update_returnsUpdatedProfile() throws Exception {
        Trainer trainer = trainer();
        trainer.getUser().setFirstName("Annie");
        when(trainerService.update(eq(CREDENTIALS), any(UpdateTrainerRequest.class)))
                .thenReturn(new TrainerProfile(trainer, List.of()));

        mvc.perform(put("/api/trainers/" + ANNA).header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Annie\",\"lastName\":\"Lee\",\"isActive\":true,\"specialization\":{\"id\":5}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Annie"))
                .andExpect(jsonPath("$.specialization.id").value(2))
                .andExpect(jsonPath("$.trainees").isEmpty());

        verify(trainerService).update(CREDENTIALS, new UpdateTrainerRequest("Annie", "Lee", true));
    }

    @Test
    void update_requiresNamesAndIsActive() throws Exception {
        mvc.perform(put("/api/trainers/" + ANNA).header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainings_passesFiltersAndReturnsTrainings() throws Exception {
        when(trainingService.getTrainerTrainings(eq(CREDENTIALS), any(TrainerTrainingCriteria.class)))
                .thenReturn(List.of(training(trainee(), trainer())));

        mvc.perform(get("/api/trainers/" + ANNA + "/trainings").header("Authorization", AUTH)
                        .param("periodFrom", "2026-11-01").param("traineeName", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                .andExpect(jsonPath("$[0].traineeName").value("John Smith"))
                .andExpect(jsonPath("$[0].trainingType.name").value("Yoga"));

        verify(trainingService).getTrainerTrainings(CREDENTIALS,
                new TrainerTrainingCriteria(LocalDate.of(2026, 11, 1), null, "John"));
    }
}
