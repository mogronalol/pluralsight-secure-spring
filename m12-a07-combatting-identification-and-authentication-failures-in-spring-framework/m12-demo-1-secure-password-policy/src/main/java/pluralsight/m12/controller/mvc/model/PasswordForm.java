package pluralsight.m12.controller.mvc.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordForm {
    @NotBlank(message = "{email.invalid}")
    @Email(message = "{email.invalid}")
    private String email;
    @Size(min = 8, max = 64, message = "{password.length.invalid}")
    private String currentPassword;
    @Size(min = 8, max = 64, message = "{password.length.invalid}")
    private String newPassword;
    @Size(min = 8, max = 64, message = "{password.length.invalid}")
    private String confirmPassword;
}
