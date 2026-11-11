package com.redbeanz.backend.auth;

import com.redbeanz.backend.user.User;
import com.redbeanz.backend.user.UserRepository;
import java.time.Instant;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthService {
    private static final long ACCESS_TOKEN_SECONDS = 3600;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String unknownUserPasswordHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.unknownUserPasswordHash = passwordEncoder.encode("redbeanz-unknown-user");
    }

    public LoginResponse login(LoginRequest request) {
        String loginId = request.loginId().toLowerCase(Locale.ROOT);
        User user = userRepository.findByLoginId(loginId).orElse(null);

        String passwordHash = unknownUserPasswordHash;
        if (user != null) {
            passwordHash = user.getPasswordHash();
        }
        // Check BCrypt even when the login ID does not exist.
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);
        if (user == null || !passwordMatches) {
            throw new BadCredentialsException("Invalid login ID or password");
        }

        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(JwtConfig.ISSUER)
            .subject(user.getId().toString())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(ACCESS_TOKEN_SECONDS))
            .claim("role", user.getRole().name())
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);
        String accessToken = jwtEncoder.encode(parameters).getTokenValue();
        return new LoginResponse(accessToken, "Bearer", ACCESS_TOKEN_SECONDS);
    }
}
