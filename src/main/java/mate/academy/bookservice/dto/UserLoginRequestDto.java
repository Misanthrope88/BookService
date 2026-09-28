package mate.academy.bookservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
        @NotBlank(message = "Password must not be blank")
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String password
) {
}
