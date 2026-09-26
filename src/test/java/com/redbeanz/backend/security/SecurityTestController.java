package com.redbeanz.backend.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

    @GetMapping("/security-test/public")
    public String publicEndpoint() {
        return "public";
    }

    @GetMapping("/security-test/user")
    public String userEndpoint() {
        return "user";
    }

    @GetMapping("/security-test/admin")
    public String adminEndpoint() {
        return "admin";
    }
}
