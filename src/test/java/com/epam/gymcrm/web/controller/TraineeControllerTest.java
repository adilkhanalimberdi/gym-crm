package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.exception.AccessDeniedException;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.TraineeService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@Import(ResponseMapper.class)
class TraineeControllerTest {

    private static final Credentials CREDENTIALS = new Credentials(JOHN, "secret");
    private static final String AUTH = basic(JOHN, "secret");

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TraineeService traineeService;
    @MockitoBean
    private TrainingService trainingService;

    @Test
    void register_returnsGeneratedCredentials() throws Exception {
        when(traineeService.create(any(CreateTraineeRequest.class))).thenReturn(trainee());

        mvc.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith","dateOfBirth":"1992-07-20","address":"7 River Road"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trainees/John.Smith"))
                .andExpect(jsonPath("$.username").value(JOHN))
                .andExpect(jsonPath("$.password").value("secret"));

        verify(traineeService).create(new CreateTraineeRequest("John", "Smith", LocalDate.of(1992, 7, 20), "7 River Road"));
    }

    @Test
    void register_acceptsMissingOptionalFields() throws Exception {
        when(traineeService.create(any(CreateTraineeRequest.class))).thenReturn(trainee());

        mvc.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void register_rejectsMissingRequiredFields() throws Exception {
        mvc.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("firstName")))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("lastName")));

        verifyNoInteractions(traineeService);
    }

    @Test
    void register_rejectsDateOfBirthInTheFuture() throws Exception {
        mvc.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\",\"dateOfBirth\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("dateOfBirth")));
    }

    @Test
    void register_rejectsMalformedJson() throws Exception {
        mvc.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void getProfile_returnsProfileWithTrainers() throws Exception {
        Trainee trainee = trainee();
        trainee.replaceTrainers(List.of(trainer()));
        when(traineeService.getProfile(CREDENTIALS)).thenReturn(trainee);

        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(JOHN))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.dateOfBirth").value("1992-07-20"))
                .andExpect(jsonPath("$.address").value("7 River Road"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.trainers[0].username").value(ANNA))
                .andExpect(jsonPath("$.trainers[0].specialization.name").value("Yoga"));
    }

    @Test
    void getProfile_requiresCredentials() throws Exception {
        mvc.perform(get("/api/trainees/" + JOHN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"gym-crm\""))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verifyNoInteractions(traineeService);
    }

    @Test
    void getProfile_rejectsMalformedAuthorizationHeaders() throws Exception {
        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", "Bearer abc"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", "Basic !!!not-base64"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", "Basic bm9jb2xvbg=="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_isUnauthorized_whenPasswordIsWrong() throws Exception {
        when(traineeService.getProfile(any())).thenThrow(new AuthenticationFailedException());

        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", basic(JOHN, "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_isForbidden_forSomeoneElsesProfile() throws Exception {
        mvc.perform(get("/api/trainees/Kate.Brown").header("Authorization", AUTH))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(traineeService);
    }

    @Test
    void update_returnsUpdatedProfile() throws Exception {
        Trainee updated = trainee();
        updated.getUser().setActive(false);
        when(traineeService.update(eq(CREDENTIALS), any(UpdateTraineeRequest.class))).thenReturn(updated);

        mvc.perform(put("/api/trainees/" + JOHN).header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith","dateOfBirth":"1992-07-20","address":"7 River Road","isActive":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.trainers").isEmpty());

        verify(traineeService).update(CREDENTIALS,
                new UpdateTraineeRequest("John", "Smith", LocalDate.of(1992, 7, 20), "7 River Road", false));
    }

    @Test
    void update_requiresIsActive() throws Exception {
        mvc.perform(put("/api/trainees/" + JOHN).header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("isActive")));
    }

    @Test
    void delete_removesProfile() throws Exception {
        mvc.perform(delete("/api/trainees/" + JOHN).header("Authorization", AUTH))
                .andExpect(status().isOk());

        verify(traineeService).delete(CREDENTIALS);
    }

    @Test
    void getUnassignedTrainers_returnsTrainers() throws Exception {
        when(traineeService.getUnassignedTrainers(CREDENTIALS)).thenReturn(List.<Trainer>of(trainer()));

        mvc.perform(get("/api/trainees/" + JOHN + "/unassigned-trainers").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(ANNA))
                .andExpect(jsonPath("$[0].firstName").value("Anna"))
                .andExpect(jsonPath("$[0].specialization.id").value(2));
    }

    @Test
    void updateTrainers_returnsNewTrainerList() throws Exception {
        Trainee trainee = trainee();
        trainee.replaceTrainers(List.of(trainer()));
        when(traineeService.updateTrainers(eq(CREDENTIALS), any())).thenReturn(trainee);

        mvc.perform(put("/api/trainees/" + JOHN + "/trainers").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainers\":[{\"username\":\"Anna.Lee\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(ANNA));

        verify(traineeService).updateTrainers(CREDENTIALS, List.of(ANNA));
    }

    @Test
    void updateTrainers_requiresTrainerList() throws Exception {
        mvc.perform(put("/api/trainees/" + JOHN + "/trainers").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/trainees/" + JOHN + "/trainers").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"trainers\":[{\"username\":\"\"}]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void updateTrainers_isNotFound_forUnknownTrainer() throws Exception {
        when(traineeService.updateTrainers(eq(CREDENTIALS), any()))
                .thenThrow(new EntityNotFoundException("Trainer", "Ghost.Trainer"));

        mvc.perform(put("/api/trainees/" + JOHN + "/trainers").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainers\":[{\"username\":\"Ghost.Trainer\"}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainer 'Ghost.Trainer' was not found"));
    }

    @Test
    void getTrainings_passesFiltersAndReturnsTrainings() throws Exception {
        Trainee trainee = trainee();
        when(trainingService.getTraineeTrainings(eq(CREDENTIALS), any(TraineeTrainingCriteria.class)))
                .thenReturn(List.of(training(trainee, trainer())));

        mvc.perform(get("/api/trainees/" + JOHN + "/trainings").header("Authorization", AUTH)
                        .param("periodFrom", "2026-11-01").param("periodTo", "2026-11-30")
                        .param("trainerName", "Anna").param("trainingType", "Yoga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-11-02"))
                .andExpect(jsonPath("$[0].trainingType.name").value("Yoga"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].trainerName").value("Anna Lee"));

        verify(trainingService).getTraineeTrainings(CREDENTIALS,
                new TraineeTrainingCriteria(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "Anna", "Yoga"));
    }

    @Test
    void getTrainings_filtersAreOptional() throws Exception {
        mvc.perform(get("/api/trainees/" + JOHN + "/trainings").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(trainingService).getTraineeTrainings(CREDENTIALS, TraineeTrainingCriteria.none());
    }

    @Test
    void getTrainings_rejectsUnparsableDate() throws Exception {
        mvc.perform(get("/api/trainees/" + JOHN + "/trainings").header("Authorization", AUTH)
                        .param("periodFrom", "yesterday"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'periodFrom' has an invalid value"));
    }

    @Test
    void getTrainings_rejectsInvertedPeriod() throws Exception {
        when(trainingService.getTraineeTrainings(eq(CREDENTIALS), any()))
                .thenThrow(new InvalidRequestException("From date must not be after to date"));

        mvc.perform(get("/api/trainees/" + JOHN + "/trainings").header("Authorization", AUTH)
                        .param("periodFrom", "2026-12-01").param("periodTo", "2026-11-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedFailures_areReportedWithoutInternals() throws Exception {
        when(traineeService.getProfile(any())).thenThrow(new IllegalStateException("db password is hunter2"));

        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", AUTH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("hunter2"))));
    }

    @Test
    void accessDeniedFromService_isForbidden() throws Exception {
        when(traineeService.getProfile(any())).thenThrow(new AccessDeniedException("nope"));

        mvc.perform(get("/api/trainees/" + JOHN).header("Authorization", AUTH))
                .andExpect(status().isForbidden());
    }

    @Test
    void unsupportedMethodsAndMediaTypes_areRejected() throws Exception {
        mvc.perform(delete("/api/trainees")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/trainees").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(get("/api/unknown")).andExpect(status().isNotFound());
    }
}
