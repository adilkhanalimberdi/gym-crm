package com.epam.gymcrm.web.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.UserService;
import com.epam.gymcrm.web.controller.AuthController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class LoggingFiltersTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private UserService userService;

    private final Logger restLogger = (Logger) LoggerFactory.getLogger("rest.calls");
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void attachAppender() {
        appender.start();
        restLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        restLogger.detachAppender(appender);
    }

    @Test
    void everyResponseCarriesAGeneratedTransactionId() throws Exception {
        String id = mvc.perform(get("/api/auth/login").param("username", "u").param("password", "p"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Transaction-Id"))
                .andReturn().getResponse().getHeader("X-Transaction-Id");

        assertNotNull(id);
        assertEquals(36, id.length());
        assertNull(TransactionId.current(), "the id must not leak to the next request on the same thread");
    }

    @Test
    void harmlessTransactionIdFromTheCallerIsReused() throws Exception {
        mvc.perform(get("/api/auth/login").param("username", "u").param("password", "p")
                        .header("X-Transaction-Id", "flow-42"))
                .andExpect(header().string("X-Transaction-Id", "flow-42"));
    }

    @Test
    void suspiciousTransactionIdIsReplaced() throws Exception {
        String id = mvc.perform(get("/api/auth/login").param("username", "u").param("password", "p")
                        .header("X-Transaction-Id", "fake\nINFO forged line"))
                .andReturn().getResponse().getHeader("X-Transaction-Id");

        assertEquals(36, id.length());
    }

    @Test
    void successfulCallIsLoggedWithoutPassword() throws Exception {
        mvc.perform(get("/api/auth/login?username=John.Smith&password=topSecret1"));

        ILoggingEvent event = onlyEvent();
        assertEquals(Level.INFO, event.getLevel());
        assertTrue(event.getFormattedMessage().contains("GET /api/auth/login?username=John.Smith&password=***"));
        assertTrue(event.getFormattedMessage().contains("-> 200"));
        assertFalse(event.getFormattedMessage().contains("topSecret1"));
    }

    @Test
    void failedCallIsLoggedAsWarningWithErrorMessageAndMaskedBody() throws Exception {
        doThrow(new com.epam.gymcrm.exception.AuthenticationFailedException())
                .when(userService).changePassword(any(), any());

        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"John.Smith\",\"oldPassword\":\"oldSecret1\",\"newPassword\":\"newSecret2\"}"));

        ILoggingEvent event = onlyEvent();
        String message = event.getFormattedMessage();
        assertEquals(Level.WARN, event.getLevel());
        assertTrue(message.contains("PUT /api/auth/password -> 401"));
        assertTrue(message.contains("Invalid username or password"));
        assertTrue(message.contains("\"oldPassword\":\"***\""));
        assertFalse(message.contains("oldSecret1"));
        assertFalse(message.contains("newSecret2"));
    }

    @Test
    void nonApiRequestsAreNotLogged() throws Exception {
        mvc.perform(get("/something-else"));

        assertTrue(appender.list.isEmpty());
    }

    private ILoggingEvent onlyEvent() {
        List<ILoggingEvent> events = appender.list;
        assertEquals(1, events.size());
        return events.get(0);
    }
}
