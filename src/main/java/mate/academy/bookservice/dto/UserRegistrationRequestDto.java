package mate.academy.bookservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mate.academy.bookservice.validation.FieldMatch;

@FieldMatch(first = "password", second = "repeatPassword", message = "Passwords must match")
public record UserRegistrationRequestDto(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
        @NotBlank(message = "Password must not be blank")
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String password,
        @NotBlank(message = "Repeated password must not be blank")
        String repeatPassword,
        @NotBlank(message = "First name must not be blank")
        @Size(max = 255, message = "First name must not exceed 255 characters")
        String firstName,
        @NotBlank(message = "Last name must not be blank")
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastName,
        @Size(max = 255, message = "Shipping address must not exceed 255 characters")
        String shippingAddress
) {
}
