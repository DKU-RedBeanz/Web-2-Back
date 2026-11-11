package com.redbeanz.backend.config;

import com.redbeanz.backend.auth.JwtConfig;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration
@Import(JwtConfig.class)
public class SecurityConfig {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder,
            @Qualifier("apiCorsConfigurationSource") CorsConfigurationSource apiCors) throws Exception {
        RequestMatcher login = pathPattern(HttpMethod.POST, "/api/auth/login");
        RequestMatcher publicEndpoints = new OrRequestMatcher(
            login,
            pathPattern(HttpMethod.POST, "/api/users"),
            pathPattern(HttpMethod.GET, "/api/users/csrf"),
            pathPattern("/swagger-ui.html"),
            pathPattern("/swagger-ui/**"),
            pathPattern("/v3/api-docs"),
            pathPattern("/v3/api-docs/**"),
            pathPattern("/security-test/public")
        );
        BearerTokenAuthenticationConverter bearerTokens = new BearerTokenAuthenticationConverter();
        BearerTokenAuthenticationEntryPoint unauthorized = new BearerTokenAuthenticationEntryPoint();
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthentication = new JwtAuthenticationConverter();
        jwtAuthentication.setJwtGrantedAuthoritiesConverter(authorities);

        http
            .cors(cors -> cors.configurationSource(apiCors))
            .csrf(csrf -> csrf.ignoringRequestMatchers(login))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(publicEndpoints).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/security-test/admin").hasRole("ADMIN")
                .requestMatchers("/security-test/user").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(server -> server
                // Public endpoints keep their own checks, including signup CSRF.
                .authenticationConverter(request -> {
                    if (publicEndpoints.matches(request)) {
                        return null;
                    }
                    return bearerTokens.convert(request);
                })
                .authenticationEntryPoint(unauthorized)
                .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthentication)))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorized)
                .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
            );

        return http.build();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    CorsConfigurationSource apiCorsConfigurationSource(@Value("${CORS_ALLOWED_ORIGINS:}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        for (String origin : allowedOrigins.split(",")) {
            if (!origin.isBlank()) {
                configuration.addAllowedOrigin(origin.strip());
            }
        }
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.validateAllowCredentials();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
