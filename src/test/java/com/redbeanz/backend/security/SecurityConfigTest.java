package com.redbeanz.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.redbeanz.backend.config.SecurityConfig;

@WebMvcTest(SecurityTestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void publicEndpointIsAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/security-test/public"))
            .andExpect(status().isOk());
    }

    @Test
    void userEndpointReturns401WithoutLogin() throws Exception {
        mockMvc.perform(get("/security-test/user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpointReturns403ForNormalUser() throws Exception {
        mockMvc.perform(get("/security-test/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpointIsAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/security-test/admin"))
            .andExpect(status().isOk());
    }
}
