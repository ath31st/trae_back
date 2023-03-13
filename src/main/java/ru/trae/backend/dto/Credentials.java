package ru.trae.backend.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import ru.trae.backend.util.RegExpression;

/**
 * Represents the login credentials for a user.
 *
 * @author Vladimir Olennikov
 */
public record Credentials(
        @NotNull(message = "Invalid username: username is NULL")
        @Pattern(regexp = RegExpression.USERNAME, message = "Invalid username format")
        String username,
        @NotNull(message = "Invalid password: password is NULL")
        @Pattern(regexp = RegExpression.PASSWORD, message = "Invalid password format")
        String password
) {
}