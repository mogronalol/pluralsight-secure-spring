package pluralsight.m15.controller.mvc.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "{email.invalid}")
    @Email(message = "{email.invalid}")
    private String email;
    @Size(min = 8, max = 64, message = "{password.length.invalid}")
    private String password;
    @Size(min = 8, max = 64, message = "{password.length.invalid}")
    private String confirmPassword;
}
