package com.careerplanning.backend.modules.auth.controller;

import com.careerplanning.backend.modules.auth.dto.AuthResponse;
import com.careerplanning.backend.modules.auth.dto.GoogleSignInRequest;
import com.careerplanning.backend.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void googleSignInEndpointReturnsApiResponseEnvelope() throws Exception {
        when(authService.googleSignIn(new GoogleSignInRequest("google-id-token")))
                .thenReturn(new AuthResponse(12L, "uuid-token"));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "google-id-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(12))
                .andExpect(jsonPath("$.data.token").value("uuid-token"));
    }
}
