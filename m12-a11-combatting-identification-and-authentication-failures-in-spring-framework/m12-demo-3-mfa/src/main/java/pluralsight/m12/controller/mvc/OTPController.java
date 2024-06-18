package pluralsight.m12.controller.mvc;

import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OTPController {

    @GetMapping("/otp")
    public String showOtpForm(Model model) {
        model.addAttribute("otpForm", new OTPForm());
        return "otp";
    }

    @PostMapping("/otp-validation")
    public String validateOtp(@Valid @ModelAttribute("otpForm") OTPForm otpForm,
                              BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "otp";
        }

        if (!isValidOtp(otpForm.getOtp())) {
            bindingResult.rejectValue("otp", "error.otpForm", "Invalid or expired OTP");
            return "otp";
        }


        Authentication updatedAuth = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                ((UserDetails)authentication.getPrincipal()).getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(updatedAuth);

        return "redirect:/";
    }

    private boolean isValidOtp(String otp) {
        return true;
    }
}
