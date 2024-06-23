package pluralsight.m12.controller.mvc;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pluralsight.m12.controller.mvc.model.InitiatePasswordResetForm;
import pluralsight.m12.controller.mvc.model.PasswordForm;
import pluralsight.m12.domain.ValidationError;
import pluralsight.m12.service.UserService;

import java.util.Set;

@Controller
@RequestMapping("/reset-password")
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;

    @GetMapping("/initiate")
    public String initiateResetPassword(Model model, final HttpSession session) {
        model.addAttribute("passwordResetForm", new InitiatePasswordResetForm());
        model.addAttribute("reset", false);
        final Object compromisedPassword = session.getAttribute("compromisedPassword");
        if (compromisedPassword != null && compromisedPassword.equals(true)) {
            model.addAttribute("compromisedPassword", true);
            session.removeAttribute("compromisedPassword");
        }
        return "initiate-password-reset";
    }

    @PostMapping("/initiate")
    public String initiateResetPassword(
            @Valid @ModelAttribute("passwordResetForm") PasswordForm form,
            BindingResult result,
            Model model) {

        if (!result.hasErrors()) {
            userService.triggerPasswordReset(form.getEmail());
        }

        model.addAttribute("reset", !result.hasErrors());

        return "initiate-password-reset";
    }

    @GetMapping
    public String resetPassword(@RequestParam("token") String token, Model model) {

        model.addAttribute("token", token);

        model.addAttribute("passwordForm", new PasswordForm());
        return "reset-password";
    }

    @PostMapping
    public String resetPassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("newPassword", "password.mismatch");
            result.rejectValue("confirmPassword", "password.mismatch");
            return "reset-password";
        }

        final Set<ValidationError> validationErrors =
                userService.updatePassword(
                        form.getEmail(),
                        form.getResetToken(),
                        form.getCurrentPassword(),
                        form.getNewPassword()
                );

        if (validationErrors.isEmpty()) {
            redirectAttributes.addFlashAttribute("success",
                    "Your password has been successfully reset.");
            return "redirect:/login";
        } else {

            if (validationErrors.contains(ValidationError.WRONG_PASSWORD)) {
                result.rejectValue("currentPassword", "password.incorrect");
            }

            if (validationErrors.contains(ValidationError.PASSWORD_COMPROMISED)) {
                result.rejectValue("newPassword", "password.compromised");
            }

            if (validationErrors.contains(ValidationError.WRONG_OR_EXPIRED_TOKEN)) {
                result.rejectValue("resetToken", "token.reset.invalid");
            }

            return "reset-password";
        }
    }
}
