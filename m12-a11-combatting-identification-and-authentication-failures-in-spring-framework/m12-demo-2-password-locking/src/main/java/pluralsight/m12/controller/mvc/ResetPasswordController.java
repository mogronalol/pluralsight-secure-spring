package pluralsight.m12.controller.mvc;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reset-password")
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordController {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final MessageSource messageSource;

    @GetMapping
    public String resetPassword(Model model, HttpSession session) {

        final Object compromisedPassword = session.getAttribute("compromisedPassword");

        if (compromisedPassword != null && compromisedPassword.equals(true)) {
            model.addAttribute("compromisedPassword", true);
            session.removeAttribute("compromisedPassword");
        }

        model.addAttribute("passwordForm", new PasswordForm());
        return "reset-password";
    }

    @PostMapping
    public String resetPassword(@Valid @ModelAttribute("passwordForm") PasswordForm form, BindingResult result, RedirectAttributes redirectAttributes) {

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("newPassword", "password.mismatch");
            result.rejectValue("confirmPassword", "password.mismatch");
        }

        if (userDetailsManager.userExists(form.getEmail())) {

            final UserDetails userDetails =
                    userDetailsManager.loadUserByUsername(form.getEmail());

            if (!passwordEncoder.matches(form.getCurrentPassword(), userDetails.getPassword())) {

                result.rejectValue("currentPassword", "password.incorrect");
            }

            if (!result.hasFieldErrors("newPassword") && compromisedPasswordChecker.check(form.getNewPassword()).isCompromised()) {
                result.rejectValue("newPassword", "password.compromised");
            }

        } else if(!form.getEmail().isBlank()) {
            result.rejectValue("email", "email.does.not.exist");
        }

        if (result.hasErrors()) {
            return "reset-password";
        }

        userDetailsManager.updateUser(User.withUserDetails(userDetailsManager.loadUserByUsername(form.getEmail()))
                .password(passwordEncoder.encode(form.getNewPassword())).
                build());

        redirectAttributes.addFlashAttribute("success", "Your password has been successfully reset.");
        return "redirect:/login";
    }
}
