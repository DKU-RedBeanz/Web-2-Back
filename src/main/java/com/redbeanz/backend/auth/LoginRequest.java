package com.redbeanz.backend.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public record LoginRequest(
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_]{3,30}")
    @JsonDeserialize(using = JsonStringDeserializer.class)
    String loginId,

    @NotBlank
    @Size(max = 72)
    @JsonDeserialize(using = JsonStringDeserializer.class)
    String password
) {
    @AssertTrue
    @JsonIgnore
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || (password.indexOf('\0') < 0
            && password.getBytes(StandardCharsets.UTF_8).length <= 72);
    }

    @Override
    public String toString() {
        return "LoginRequest[REDACTED]";
    }

    static final class JsonStringDeserializer extends ValueDeserializer<String> {
        @Override
        public String deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return context.reportInputMismatch(String.class, "Expected a JSON string");
            }
            return parser.getString();
        }
    }
}
