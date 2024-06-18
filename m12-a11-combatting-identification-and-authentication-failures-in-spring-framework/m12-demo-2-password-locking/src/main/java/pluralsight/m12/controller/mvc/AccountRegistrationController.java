package pluralsight.m12.controller.mvc;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.userdetails.User;
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
@RequestMapping("/account-registration")
@AllArgsConstructor
public class AccountRegistrationController {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;

    @GetMapping
    public String accountsRegistration(Model model) {
        model.addAttribute("userForm", new UserRegistrationDto());
        return "account-registration";
    }

    @PostMapping
    public String registerUserAccount(@Valid @ModelAttribute("userForm") UserRegistrationDto userDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("password",  "password.mismatch");
        }

        if (userDetailsManager.userExists(userDto.getEmail())) {
            result.rejectValue("email", "email.already.exists");
        }

        if (!result.hasFieldErrors("password") && compromisedPasswordChecker.check(userDto.getPassword()).isCompromised()) {
            result.rejectValue("password", "password.compromised");
        }

        if (result.hasErrors()) {
            return "account-registration";
        }

        userDetailsManager.createUser(User.builder()
                        .username(userDto.getEmail())
                        .password(passwordEncoder.encode(userDto.getPassword()))
                .build());

        redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful. Please login.");

        return "redirect:/login";
    }

}
