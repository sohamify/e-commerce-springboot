package com.example.ecommerce.auth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ecommerce.TestcontainersConfiguration;
import com.example.ecommerce.notification.EmailMessage;
import com.example.ecommerce.notification.NotificationSender;
import jakarta.servlet.http.Cookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Exercises the full onboarding journey end to end through the real HTTP layer, against a real
 * (Testcontainers) Postgres instance — register, verify, login, refresh/rotate, forgot/reset —
 * matching the manual curl walkthrough in the plan's verification section.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

    private static final Pattern TOKEN_LINK_PATTERN = Pattern.compile("token=([^\"&]+)");
    private static final Pattern ACCESS_TOKEN_FIELD_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationSender notificationSender;

    @Test
    void fullOnboardingJourney() throws Exception {
        String email = "flow-test@example.com";
        String password = "correct-horse-1";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email, password)))
            .andExpect(status().isCreated());

        String verificationToken = captureAndExtractToken();

        // Unverified accounts can't log in yet.
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email, password)))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"%s\"}".formatted(verificationToken)))
            .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn();

        String accessToken = extractField(loginResult.getResponse().getContentAsString(), ACCESS_TOKEN_FIELD_PATTERN);
        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");
        assertNotNull(refreshCookie);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email));

        // Unauthenticated access is rejected.
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
            .andExpect(status().isOk())
            .andReturn();
        Cookie rotatedCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertNotNull(rotatedCookie);
        assertNotEquals(refreshCookie.getValue(), rotatedCookie.getValue());

        // Reusing the now-rotated-away token is rejected (theft-detection).
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
            .andExpect(status().isUnauthorized());

        reset(notificationSender);
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\"}".formatted(email)))
            .andExpect(status().isOk());

        String resetToken = captureAndExtractToken();
        String newPassword = "new-correct-horse-1";

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"%s\",\"newPassword\":\"%s\"}".formatted(resetToken, newPassword)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email, password)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email, newPassword)))
            .andExpect(status().isOk());

        // Resetting the password must revoke sessions that were alive before the reset.
        mockMvc.perform(post("/api/auth/refresh").cookie(rotatedCookie))
            .andExpect(status().isUnauthorized());
    }

    private String captureAndExtractToken() {
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(notificationSender).send(captor.capture());
        return URLDecoder.decode(extractField(captor.getValue().htmlBody(), TOKEN_LINK_PATTERN), StandardCharsets.UTF_8);
    }

    private String extractField(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        assertTrue(matcher.find(), "expected to find " + pattern + " in: " + source);
        return matcher.group(1);
    }

    private String json(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }
}
