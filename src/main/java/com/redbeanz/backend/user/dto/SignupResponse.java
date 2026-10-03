package com.redbeanz.backend.user.dto;

import com.redbeanz.backend.user.User;
import com.redbeanz.backend.user.UserRole;

public record SignupResponse(Long id, String loginId, String email, String nickname, UserRole role) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getLoginId(), user.getEmail(),
            user.getNickname(), user.getRole());
    }
}
