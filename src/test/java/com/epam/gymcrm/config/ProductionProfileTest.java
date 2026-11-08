package com.epam.gymcrm.config;

import com.epam.gymcrm.dao.TrainingTypeDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Starts the application with the prod profile on H2 in PostgreSQL mode. Startup itself proves that the
 * Flyway migration matches the entities, because Hibernate validates the schema it created.
 */
@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:prod-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "DB_USERNAME=sa",
        "DB_PASSWORD="
})
@ActiveProfiles("prod")
@AutoConfigureMockMvc
@AutoConfigureObservability(tracing = false)
class ProductionProfileTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void migrationLoadsTrainingTypes() {
        assertEquals(5, trainingTypeDao.findAll().size());
    }

    @Test
    void health_hidesDetails() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void prometheus_isPublished() throws Exception {
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    }

    @Test
    void metricsEndpoint_isNotPublished() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isNotFound());
    }
}
