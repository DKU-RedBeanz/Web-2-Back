package com.redbeanz.backend.user.dto;

import com.redbeanz.backend.user.User;
import com.redbeanz.backend.user.UserRole;

public record UserProfileResponse(Long id, String loginId, String email, String nickname, UserRole role) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getId(), user.getLoginId(), user.getEmail(),
            user.getNickname(), user.getRole());
    }
}
