package pluralsight.m12.controller.mvc;


import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OTPForm {
    @Size(min = 6, max = 6, message = "{invalid.otp}")
    private String otp;
}
