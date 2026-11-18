package com.redbeanz.backend.user;

import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Signup-only error handling; never include rejected values, SQL or exception text.
@RestControllerAdvice(assignableTypes = UserController.class)
public class SignupExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidInput(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getAllErrors().stream()
            .map(error -> error.getDefaultMessage()).distinct().sorted().toList();
        return new ErrorResponse("입력값을 확인해 주세요.", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidJson() {
        return new ErrorResponse("올바른 JSON 요청을 입력해 주세요.", List.of());
    }

    @ExceptionHandler(DuplicateUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse duplicate() {
        return new ErrorResponse("이미 사용 중인 아이디 또는 이메일입니다.", List.of());
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse databaseFailure() {
        return new ErrorResponse("회원 정보를 저장하지 못했습니다.", List.of());
    }

    public record ErrorResponse(String message, List<String> errors) {
    }
}
