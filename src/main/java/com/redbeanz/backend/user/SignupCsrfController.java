package com.redbeanz.backend.user;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignupCsrfController {
    // Anonymous clients keep the session cookie and send this token with signup.
    @GetMapping("/api/users/csrf")
    public ResponseEntity<CsrfResponse> csrf(CsrfToken token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new CsrfResponse(token.getHeaderName(), token.getToken()));
    }

    public record CsrfResponse(String headerName, String token) {
    }
}
