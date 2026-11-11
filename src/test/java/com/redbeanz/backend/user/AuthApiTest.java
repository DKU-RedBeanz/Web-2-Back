package com.redbeanz.backend.user;

import com.redbeanz.backend.auth.AuthController;
import com.redbeanz.backend.auth.AuthService;
import com.redbeanz.backend.config.SecurityConfig;
import com.redbeanz.backend.security.SecurityTestController;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, UserController.class, UserProfileController.class, SecurityTestController.class})
@Import({SecurityConfig.class, AuthService.class, UserService.class})
@TestPropertySource(locations = "/jwt-test.properties", properties =
    "CORS_ALLOWED_ORIGINS=http://localhost:5173, https://app.redbeanz.example")
class AuthApiTest {
    private static final String LOGIN_ID = "beanuser";
    private static final String PASSWORD = "ExamplePass123!";
    private static final long USER_ID = 17L;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired JwtDecoder jwtDecoder;
    @MockitoBean UserRepository repository;

    @Test
    void validLoginWithoutCsrfReturnsBearerAccessToken() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);

        var result = mvc.perform(login("BEANUSER", PASSWORD))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
            .andReturn();

        var jwt = jwtDecoder.decode(accessToken(result));
        assertThat(jwt.getSubject()).isEqualTo("17");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(1));
        assertThat(jwt.getClaims()).doesNotContainKeys("password", "passwordHash", "email");
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}", "null", "{malformed",
        "{\"loginId\":\"bad id\",\"password\":\"ExamplePass123!\"}",
        "{\"loginId\":\"beanuser\",\"password\":\" \"}",
        "{\"loginId\":\"beanuser\",\"password\":[]}",
        "{\"loginId\":12345,\"password\":\"ExamplePass123!\"}",
        "{\"loginId\":\"beanuser\",\"password\":true}"
    })
    void malformedOrInvalidLoginReturns400(String body) throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void bcryptVerifiesPasswordAt72Utf8BytesWithoutTrimming() throws Exception {
        String password = " " + "가".repeat(23) + "  ";
        stubAccount(UserRole.USER, password);

        mvc.perform(login(LOGIN_ID, password)).andExpect(status().isOk());
    }

    @Test
    void passwordOver72Utf8BytesIsRejectedBeforeBcryptCanTruncateIt() throws Exception {
        String password = "가".repeat(24);
        stubAccount(UserRole.USER, password);

        mvc.perform(login(LOGIN_ID, password + "x")).andExpect(status().isBadRequest());
    }

    @Test
    void passwordContainingNulReturns400() throws Exception {
        mvc.perform(login(LOGIN_ID, PASSWORD + '\0')).andExpect(status().isBadRequest());
    }

    @Test
    void wrongPasswordAndUnknownIdReturnTheSame401() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);

        var wrongPassword = mvc.perform(login(LOGIN_ID, "wrong password"))
            .andExpect(status().isUnauthorized()).andReturn();
        var unknownId = mvc.perform(login("missing_user", PASSWORD))
            .andExpect(status().isUnauthorized()).andReturn();

        assertThat(wrongPassword.getResponse().getContentAsString())
            .isEqualTo(unknownId.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void meReturnsTheProfileOfTheTokenSubject(UserRole role) throws Exception {
        User user = stubAccount(role, PASSWORD);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        String token = loginAndGetToken();

        mvc.perform(get("/api/users/me").param("id", "999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(17))
            .andExpect(jsonPath("$.loginId").value("beanuser"))
            .andExpect(jsonPath("$.email").value("bean@example.com"))
            .andExpect(jsonPath("$.nickname").value("레드빈"))
            .andExpect(jsonPath("$.role").value(role.name()))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginDoesNotAuthenticateAnExistingSessionWithoutBearerToken() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);
        var session = new MockHttpSession();
        mvc.perform(login(LOGIN_ID, PASSWORD).session(session)).andExpect(status().isOk());

        mvc.perform(get("/api/users/me").session(session))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    @Test
    void expiredBearerHeaderDoesNotBlockPublicLogin() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);
        String expiredToken = signedToken("redbeanz-backend", "17", "USER", Instant.now().minusSeconds(10));

        mvc.perform(login(LOGIN_ID, PASSWORD).header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void signupStillRequiresCsrfWhenBearerHeaderIsPresent() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);
        String token = loginAndGetToken();

        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{}")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void meReturns401WhenTheAuthenticatedAccountNoLongerExists() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);
        String token = loginAndGetToken();

        mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        expectInvalidToken(signedToken("redbeanz-backend", "17", "USER", Instant.now().minusSeconds(10)));
    }

    @Test
    void tokenWithoutExpirationReturns401() throws Exception {
        expectInvalidToken(signedToken("redbeanz-backend", "17", "USER", null));
    }

    @Test
    void tokenFromAnotherIssuerReturns401() throws Exception {
        expectInvalidToken(signedToken("other-issuer", "17", "USER", Instant.now().plusSeconds(3600)));
    }

    @Test
    void tokenWithInvalidUserIdReturns401() throws Exception {
        expectInvalidToken(signedToken("redbeanz-backend", "invalid", "USER", Instant.now().plusSeconds(3600)));
    }

    @Test
    void tokenWithUnsupportedRoleReturns401() throws Exception {
        expectInvalidToken(signedToken("redbeanz-backend", "17", "OTHER", Instant.now().plusSeconds(3600)));
    }

    @Test
    void tokenWithNonStringRoleReturns401() throws Exception {
        expectInvalidToken(signedToken("redbeanz-backend", "17", List.of("ADMIN"), Instant.now().plusSeconds(3600)));
    }

    @Test
    void tokenWithModifiedSignatureReturns401() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);
        String token = loginAndGetToken();
        int signatureStart = token.lastIndexOf('.') + 1;
        char replacement = token.charAt(signatureStart) == 'A' ? 'B' : 'A';
        String modified = token.substring(0, signatureStart) + replacement + token.substring(signatureStart + 1);

        expectInvalidToken(modified);
    }

    @Test
    void userTokenCannotAccessAdminRoute() throws Exception {
        stubAccount(UserRole.USER, PASSWORD);

        mvc.perform(get("/security-test/admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + loginAndGetToken()))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenCanAccessAdminRoute() throws Exception {
        stubAccount(UserRole.ADMIN, PASSWORD);

        mvc.perform(get("/security-test/admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + loginAndGetToken()))
            .andExpect(status().isOk());
    }

    @Test
    void allowedOriginCanSendBearerAuthorization() throws Exception {
        mvc.perform(options("/api/users/me")
                .header(HttpHeaders.ORIGIN, "https://app.redbeanz.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://app.redbeanz.example"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization"));
    }

    @Test
    void allowedOriginCanSendSignupCsrfHeaderWithSessionCookie() throws Exception {
        mvc.perform(options("/api/users")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type, X-CSRF-TOKEN"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("X-CSRF-TOKEN")));
    }

    @Test
    void unconfiguredOriginCannotSendLoginRequests() throws Exception {
        mvc.perform(options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, "https://unlisted.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private User stubAccount(UserRole role, String password) {
        User user = User.register(LOGIN_ID, "bean@example.com", passwordEncoder.encode(password), "레드빈");
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "role", role);
        when(repository.findByLoginId(LOGIN_ID)).thenReturn(Optional.of(user));
        return user;
    }

    private MockHttpServletRequestBuilder login(String loginId, String password) {
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("loginId", loginId, "password", password)));
    }

    private String loginAndGetToken() throws Exception {
        return accessToken(mvc.perform(login(LOGIN_ID, PASSWORD)).andExpect(status().isOk()).andReturn());
    }

    private String accessToken(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString()).path("accessToken").asText();
    }

    private void expectInvalidToken(String token) throws Exception {
        mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("invalid_token")));
    }

    private String signedToken(String issuer, String subject, Object role, Instant expiresAt) {
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(subject).claim("role", role);
        if (expiresAt != null) {
            claims.expiresAt(expiresAt);
        }
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
