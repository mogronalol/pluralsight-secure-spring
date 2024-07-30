package pluralsight.m15.controller.mvc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pluralsight.m15.controller.mvc.model.InitiatePasswordResetForm;
import pluralsight.m15.controller.mvc.model.PasswordForm;
import pluralsight.m15.domain.ValidationError;
import pluralsight.m15.service.UserService;

import java.util.Set;

@Controller
@RequestMapping("/reset-password")
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordController {

    private final UserService userService;

    @GetMapping("/initiate")
    public String initiateResetPassword(Model model) {
        model.addAttribute("passwordResetForm", new InitiatePasswordResetForm());
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
        final PasswordForm passwordForm = new PasswordForm();
        passwordForm.setResetToken(token);
        model.addAttribute("passwordForm", passwordForm);
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
                        form.getNewPassword()
                );

        if (validationErrors.isEmpty()) {
            redirectAttributes.addFlashAttribute("success",
                    "Your password has been successfully reset.");
            return "redirect:/login";
        } else {

            if (validationErrors.contains(ValidationError.COMPROMISED_PASSWORD)) {
                result.rejectValue("newPassword", "password.compromised");
            }

            if (validationErrors.contains(ValidationError.PASSWORD_RESET_INVALID)) {
                result.rejectValue("resetToken", "password.reset.invalid");
            }

            return "reset-password";
        }
    }
}
