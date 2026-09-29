package com.redbeanz.backend.user;

import com.redbeanz.backend.user.dto.SignupRequest;
import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, encoder);
    }

    private SignupRequest request() {
        return new SignupRequest("BeanUser", "Bean@Example.com", "ExamplePass123!", "레드빈");
    }

    @Test
    void hashesPasswordNormalizesIdentifiersAndFixesRole() {
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 10L);
            return user;
        });
        var response = service.signup(request());
        var saved = ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo(request().password());
        assertThat(encoder.matches(request().password(), saved.getValue().getPasswordHash())).isTrue();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.loginId()).isEqualTo("beanuser");
        assertThat(response.email()).isEqualTo("bean@example.com");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(request().toString()).doesNotContain(request().password());
    }

    @Test
    void existingLoginIdIsRejectedBeforeSaving() {
        when(repository.existsByLoginId("beanuser")).thenReturn(true);
        assertThatThrownBy(() -> service.signup(request())).isInstanceOf(DuplicateUserException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void existingEmailIsRejectedBeforeSaving() {
        when(repository.existsByEmail("bean@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.signup(request())).isInstanceOf(DuplicateUserException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void uniqueConstraintHandlesDuplicateEvenAfterPrecheck() {
        var constraint = new ConstraintViolationException("duplicate", new SQLException(),
            "users.uk_users_login_id");
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate", constraint));
        assertThatThrownBy(() -> service.signup(request())).isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void unrelatedDatabaseErrorIsNotReportedAsDuplicate() {
        var failure = new DataIntegrityViolationException("different constraint");
        when(repository.saveAndFlush(any())).thenThrow(failure);
        assertThatThrownBy(() -> service.signup(request())).isSameAs(failure);
    }
}
