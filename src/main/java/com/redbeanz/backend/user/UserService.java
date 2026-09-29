package com.redbeanz.backend.user;

import com.redbeanz.backend.user.dto.SignupRequest;
import com.redbeanz.backend.user.dto.SignupResponse;
import java.util.Locale;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String loginId = request.loginId().toLowerCase(Locale.ROOT);
        String email = request.email().toLowerCase(Locale.ROOT);
        if (userRepository.existsByLoginId(loginId) || userRepository.existsByEmail(email)) {
            throw new DuplicateUserException();
        }

        User user = User.register(loginId, email, passwordEncoder.encode(request.password()), request.nickname());
        try {
            // Flush here so a concurrent duplicate reaches this catch before returning 201.
            return SignupResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (isSignupUniqueViolation(exception)) {
                throw new DuplicateUserException();
            }
            throw exception;
        }
    }

    private boolean isSignupUniqueViolation(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String name = violation.getConstraintName();
                if (name != null) {
                    name = name.replace("`", "").replace("'", "");
                    if (name.equals("uk_users_login_id") || name.endsWith(".uk_users_login_id")
                        || name.equals("uk_users_email") || name.endsWith(".uk_users_email")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
