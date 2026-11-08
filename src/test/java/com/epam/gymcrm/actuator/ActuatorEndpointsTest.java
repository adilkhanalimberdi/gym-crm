package com.epam.gymcrm.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:actuator-it;DB_CLOSE_DELAY=-1",
        "management.endpoint.health.show-details=always"
})
@AutoConfigureMockMvc
@AutoConfigureObservability(tracing = false)
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void health_isPublished() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void health_includesCustomIndicators() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.components.trainingTypes.status").value("UP"))
                .andExpect(jsonPath("$.components.trainingTypes.details.count").value(5))
                .andExpect(jsonPath("$.components.connectionPool.status").value("UP"))
                .andExpect(jsonPath("$.components.heapMemory.status").value("UP"));
    }

    @Test
    void prometheus_publishesApplicationMetrics() throws Exception {
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }
}
