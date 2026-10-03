package com.redbeanz.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SignupRequest(
    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "[A-Za-z0-9_]{3,30}", message = "아이디는 영문, 숫자, 밑줄로 3~30자입니다.")
    String loginId,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식을 확인해 주세요.")
    @Size(max = 254, message = "이메일은 254자 이하여야 합니다.")
    @Pattern(regexp = "[\\x21-\\x7E]+", message = "이메일은 공백 없는 ASCII 문자로 입력해 주세요.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상, UTF-8 기준 72바이트 이하여야 합니다.")
    String password,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 30, message = "닉네임은 2~30자입니다.")
    @Pattern(regexp = "\\S(?:[^\\r\\n]*\\S)?", message = "닉네임 양끝의 공백과 줄바꿈은 사용할 수 없습니다.")
    String nickname
) {
    // BCrypt limits bytes, not Java string length; do not truncate a password.
    @AssertTrue(message = "비밀번호는 UTF-8 기준 72바이트 이하여야 하며 NUL 문자를 포함할 수 없습니다.")
    @JsonIgnore
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || (password.indexOf('\0') < 0
            && password.getBytes(StandardCharsets.UTF_8).length <= 72);
    }

    @Override
    public String toString() {
        return "SignupRequest[REDACTED]";
    }
}
