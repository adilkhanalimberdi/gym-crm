package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.service.TrainingTypeService;
import com.epam.gymcrm.web.mapper.ResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.epam.gymcrm.web.support.TestData.JOHN;
import static com.epam.gymcrm.web.support.TestData.basic;
import static com.epam.gymcrm.web.support.TestData.yoga;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingTypeController.class)
@Import(ResponseMapper.class)
class TrainingTypeControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TrainingTypeService trainingTypeService;

    @Test
    void getTrainingTypes_returnsIdAndName() throws Exception {
        when(trainingTypeService.findAll(new Credentials(JOHN, "secret"))).thenReturn(List.of(yoga()));

        mvc.perform(get("/api/training-types").header("Authorization", basic(JOHN, "secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Yoga"));
    }

    @Test
    void getTrainingTypes_requiresCredentials() throws Exception {
        mvc.perform(get("/api/training-types")).andExpect(status().isUnauthorized());
    }
}
