package pluralsight.m12.controller.mvc;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pluralsight.m12.controller.mvc.model.OtpForm;
import pluralsight.m12.service.AccountLockedException;
import pluralsight.m12.service.UserService;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        final Object locked = session.getAttribute("locked");

        if (locked != null && locked.equals(true)) {
            model.addAttribute("locked", true);
            session.removeAttribute("locked");
        }

        return "login";
    }

    @GetMapping("/login/otp")
    public String otpLogin() {
        return "login-otp";
    }

    @PostMapping("/login/otp")
    public String verifyOtp(@Valid @ModelAttribute("otpForm") OtpForm otpForm,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        if (userService.verifyOtp(otpForm.getOtp(), authentication.getName())) {

            final Set<SimpleGrantedAuthority> authorities =
                    userService.getUserOrError(authentication.getName())
                            .getRoles()
                            .stream()
                            .map(r -> new SimpleGrantedAuthority(r.getGrantedAuthorityName()))
                            .collect(toSet());

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return "redirect:/my-accounts";
        } else {
            model.addAttribute("error", true);
            return "login-otp";
        }
    }

    @ExceptionHandler(AccountLockedException.class)
    public String accountLockedException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("locked", true);
        return "redirect:/login";
    }
}
