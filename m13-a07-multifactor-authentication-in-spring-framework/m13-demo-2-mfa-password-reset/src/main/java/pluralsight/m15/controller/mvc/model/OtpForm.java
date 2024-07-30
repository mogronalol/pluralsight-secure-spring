package pluralsight.m15.controller.mvc.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OtpForm {
    @NotEmpty(message = "{otp.empty}")
    private String otp;
}