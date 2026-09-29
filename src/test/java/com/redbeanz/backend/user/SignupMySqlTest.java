package com.redbeanz.backend.user;

import com.redbeanz.backend.user.dto.SignupRequest;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// No @Sql, H2, schema creation, or DB reset. Apply reviewed DDL yourself first.
@Tag("signup-mysql")
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false, print = org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint.NONE)
@Transactional
class SignupMySqlTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;

    private SignupRequest request() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return new SignupRequest("test_" + suffix, suffix + "@example.com", "ExamplePass123!", "테스트회원");
    }

    private org.springframework.test.web.servlet.ResultActions signup(SignupRequest request) throws Exception {
        return mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)));
    }

    @Test
    void createsUserAndStoredHashMatches() throws Exception {
        var request = request();
        var result = signup(request).andExpect(status().isCreated())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
        Long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        entityManager.clear();
        User saved = repository.findById(id).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getPasswordHash()).isNotEqualTo(request.password());
        assertThat(encoder.matches(request.password(), saved.getPasswordHash())).isTrue();
    }

    @Test
    void invalidInputReturns400WithoutInserting() throws Exception {
        var request = request();
        long before = repository.count();
        signup(new SignupRequest(request.loginId(), "invalid", "short", request.nickname()))
            .andExpect(status().isBadRequest());
        assertThat(repository.count()).isEqualTo(before);
    }

    @Test
    void duplicateLoginIdReturns409() throws Exception {
        var first = request();
        signup(first).andExpect(status().isCreated());
        signup(new SignupRequest(first.loginId().toUpperCase(java.util.Locale.ROOT), request().email(),
            first.password(), first.nickname())).andExpect(status().isConflict());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        var first = request();
        signup(first).andExpect(status().isCreated());
        signup(new SignupRequest(request().loginId(), first.email().toUpperCase(java.util.Locale.ROOT),
            first.password(), first.nickname())).andExpect(status().isConflict());
    }

    @Test
    void mysqlItselfRejectsDuplicateLoginId() throws Exception {
        var first = request();
        signup(first).andExpect(status().isCreated());
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO users (login_id, email, password_hash, nickname, role)
            SELECT login_id, ?, password_hash, nickname, role FROM users WHERE login_id = ?
            """, request().email(), first.loginId())).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void mysqlItselfRejectsDuplicateEmail() throws Exception {
        var first = request();
        signup(first).andExpect(status().isCreated());
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO users (login_id, email, password_hash, nickname, role)
            SELECT ?, email, password_hash, nickname, role FROM users WHERE login_id = ?
            """, request().loginId(), first.loginId())).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void adminInRequestStillStoresUserRole() throws Exception {
        var request = request();
        String body = mapper.writeValueAsString(request);
        body = body.substring(0, body.length() - 1) + ",\"role\":\"ADMIN\"}";
        var result = mvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER"))
            .andReturn();
        long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        entityManager.clear();
        assertThat(repository.findById(id).orElseThrow().getRole()).isEqualTo(UserRole.USER);
    }
}
