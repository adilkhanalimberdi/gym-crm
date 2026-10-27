package com.epam.gymcrm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.epam.gymcrm.web.support.TestData.basic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Walks through the whole API against the real services and the in-memory database. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-it;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class GymCrmApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private String registerTrainee(String first, String last) throws Exception {
        return register("/api/trainees", "{\"firstName\":\"" + first + "\",\"lastName\":\"" + last
                + "\",\"dateOfBirth\":\"1992-07-20\",\"address\":\"7 River Road\"}");
    }

    private String registerTrainer(String first, String last, int specializationId) throws Exception {
        return register("/api/trainers", "{\"firstName\":\"" + first + "\",\"lastName\":\"" + last
                + "\",\"specializationId\":" + specializationId + "}");
    }

    /** Returns the Authorization header value of the new profile. */
    private String register(String url, String content) throws Exception {
        JsonNode created = body(mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated()));
        return basic(created.get("username").asText(), created.get("password").asText());
    }

    private static String username(String auth) {
        return new String(java.util.Base64.getDecoder().decode(auth.substring(6))).split(":")[0];
    }

    @Test
    void traineeAndTrainerJourney() throws Exception {
        String trainer = registerTrainer("Anna", "Lee", 2);
        String trainee = registerTrainee("John", "Smith");
        String trainerName = username(trainer);
        String traineeName = username(trainee);
        assertEquals("John.Smith", traineeName);

        // the generated username gets a serial number when it is taken
        assertEquals("John.Smith1", username(registerTrainee("John", "Smith")));

        mvc.perform(get("/api/training-types").header("Authorization", trainee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[1].name").value("Yoga"));

        mvc.perform(get("/api/trainees/" + traineeName + "/unassigned-trainers").header("Authorization", trainee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(trainerName))
                .andExpect(jsonPath("$[0].specialization.name").value("Yoga"));

        mvc.perform(put("/api/trainees/" + traineeName + "/trainers").header("Authorization", trainee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainers\":[{\"username\":\"" + trainerName + "\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(trainerName));

        mvc.perform(get("/api/trainees/" + traineeName + "/unassigned-trainers").header("Authorization", trainee))
                .andExpect(jsonPath("$").isEmpty());

        mvc.perform(get("/api/trainees/" + traineeName).header("Authorization", trainee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("7 River Road"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers[0].username").value(trainerName));

        mvc.perform(get("/api/trainers/" + trainerName).header("Authorization", trainer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainees[0].username").value(traineeName));

        mvc.perform(post("/api/trainings").header("Authorization", trainee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"traineeUsername\":\"" + traineeName + "\",\"trainerUsername\":\"" + trainerName
                                + "\",\"trainingName\":\"Morning Yoga\",\"trainingDate\":\"2026-11-02\",\"duration\":60}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/trainees/" + traineeName + "/trainings").header("Authorization", trainee)
                        .param("periodFrom", "2026-11-01").param("trainingType", "Yoga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainerName").value("Anna Lee"));
        mvc.perform(get("/api/trainers/" + trainerName + "/trainings").header("Authorization", trainer)
                        .param("periodTo", "2026-10-31"))
                .andExpect(jsonPath("$").isEmpty());

        // deactivation is not idempotent
        String deactivate = "{\"isActive\":false}";
        mvc.perform(patch("/api/users/" + traineeName + "/activation").header("Authorization", trainee)
                .contentType(MediaType.APPLICATION_JSON).content(deactivate)).andExpect(status().isOk());
        mvc.perform(patch("/api/users/" + traineeName + "/activation").header("Authorization", trainee)
                .contentType(MediaType.APPLICATION_JSON).content(deactivate)).andExpect(status().isConflict());

        mvc.perform(put("/api/trainers/" + trainerName).header("Authorization", trainer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Annie\",\"lastName\":\"Lee\",\"isActive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Annie"))
                .andExpect(jsonPath("$.specialization.name").value("Yoga"))
                .andExpect(jsonPath("$.trainees[0].username").value(traineeName));

        mvc.perform(delete("/api/trainees/" + traineeName).header("Authorization", trainee)).andExpect(status().isOk());
        mvc.perform(get("/api/trainees/" + traineeName).header("Authorization", trainee))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/trainers/" + trainerName + "/trainings").header("Authorization", trainer))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void loginAndPasswordChange() throws Exception {
        String auth = registerTrainer("Mark", "Taylor", 1);
        String user = username(auth);
        String password = new String(java.util.Base64.getDecoder().decode(auth.substring(6))).split(":")[1];

        mvc.perform(get("/api/auth/login").param("username", user).param("password", password))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login").param("username", user).param("password", "wrong"))
                .andExpect(status().isUnauthorized());

        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"oldPassword\":\"" + password
                                + "\",\"newPassword\":\"brandNew1\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/auth/login").param("username", user).param("password", password))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/login").param("username", user).param("password", "brandNew1"))
                .andExpect(status().isOk());
    }

    @Test
    void trainerCannotBeRegisteredWithUnknownSpecialization_andErrorsCarryTransactionId() throws Exception {
        mvc.perform(post("/api/trainers").header("X-Transaction-Id", "it-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Lee\",\"specializationId\":999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.transactionId").value("it-1"))
                .andExpect(jsonPath("$.message").value("Training type '999' was not found"));
    }
}
