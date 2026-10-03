package com.redbeanz.backend.user;

import com.redbeanz.backend.config.SecurityConfig;
import com.redbeanz.backend.user.dto.SignupRequest;
import com.redbeanz.backend.user.dto.SignupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserController.class, SignupCsrfController.class})
@Import(SecurityConfig.class)
class UserControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean UserService service;

    private static final String VALID = """
        {"loginId":"beanuser","email":"bean@example.com","password":"ExamplePass123!","nickname":"레드빈"}
        """;

    private void stubSuccess() {
        when(service.signup(any())).thenReturn(new SignupResponse(1L, "beanuser", "bean@example.com", "레드빈", UserRole.USER));
    }

    @Test
    void anonymousSignupReturns201WithoutPasswordFields() throws Exception {
        stubSuccess();
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(VALID))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void actualCsrfEndpointTokenAndCookieAllowAnonymousSignup() throws Exception {
        stubSuccess();
        var tokenResult = mvc.perform(get("/api/users/csrf"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
            .andReturn();
        var body = mapper.readTree(tokenResult.getResponse().getContentAsString());
        var session = (MockHttpSession) tokenResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mvc.perform(post("/api/users").session(session)
                .header(body.get("headerName").asText(), body.get("token").asText())
                .contentType(MediaType.APPLICATION_JSON).content(VALID))
            .andExpect(status().isCreated());
    }

    @Test
    void missingCsrfIsForbiddenAndDoesNotCallService() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void otherUserMethodsAreNotPublic() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"loginId\":\"ab\",\"email\":\"bad\",\"password\":\"short\",\"nickname\":\" \"}",
        "{\"loginId\":\"bad id\",\"email\":\"bean@example.com\",\"password\":\"ExamplePass123!\",\"nickname\":\"레드빈\"}",
        "null",
        "{malformed"
    })
    void invalidRequestsReturn400(String input) throws Exception {
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(not(containsString("ExamplePass123!"))));
        verifyNoInteractions(service);
    }

    @Test
    void multibytePasswordOver72BytesReturns400WithoutEchoingIt() throws Exception {
        String password = "가".repeat(25);
        var request = new SignupRequest("beanuser", "bean@example.com", password, "레드빈");
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()).andExpect(content().string(not(containsString(password))));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"loginId", "email", "password", "nickname"})
    void overlongFieldsReturn400(String field) throws Exception {
        var input = mapper.readTree(VALID);
        ((tools.jackson.databind.node.ObjectNode) input).put(field, "a".repeat(255));
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(input)))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void passwordAtUtf8ByteBoundaryIsAccepted() throws Exception {
        stubSuccess();
        var request = new SignupRequest("beanuser", "bean@example.com", "가".repeat(24), "레드빈");
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.signup(any())).thenThrow(new DuplicateUserException());
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(VALID))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void clientCannotChooseAdministratorRole() throws Exception {
        stubSuccess();
        mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("\"nickname\":", "\"role\":\"ADMIN\",\"nickname\":")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER"));
    }
}
