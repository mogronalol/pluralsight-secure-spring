package pluralsight.m12.controller.mvc;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pluralsight.m12.controller.mvc.model.UserRegistrationDto;
import pluralsight.m12.domain.ValidationError;
import pluralsight.m12.service.UserService;

import java.util.Set;

@Controller
@RequestMapping("/account-registration")
@AllArgsConstructor
public class AccountRegistrationController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;

    @GetMapping
    public String accountsRegistration(Model model) {
        model.addAttribute("userForm", new UserRegistrationDto());
        return "account-registration";
    }

    @PostMapping
    public String registerUserAccount(
            @Valid @ModelAttribute("userForm") UserRegistrationDto userDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("password", "password.mismatch");
        }

        if (!result.hasErrors()) {
            final Set<ValidationError> errors =
                    userService.createUser(userDto.getEmail(), userDto.getPassword());

            if (errors.contains(ValidationError.USER_ALREADY_EXISTS)) {
                result.rejectValue("email", "email.already.exists");
            }

            if (errors.contains(ValidationError.PASSWORD_COMPROMISED)) {
                result.rejectValue("password", "password.compromised");
            }
        }

        if (result.hasErrors()) {
            return "account-registration";
        }

        redirectAttributes.addFlashAttribute("registrationSuccess",
                "Registration successful. Please login.");

        return "redirect:/login";
    }

}
