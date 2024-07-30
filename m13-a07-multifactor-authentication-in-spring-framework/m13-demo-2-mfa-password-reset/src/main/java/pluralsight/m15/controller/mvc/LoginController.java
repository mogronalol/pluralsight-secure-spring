package pluralsight.m15.controller.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pluralsight.m15.controller.mvc.model.OtpForm;
import pluralsight.m15.service.AccountLockedException;
import pluralsight.m15.service.UserService;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class LoginController {

    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login/otp")
    public String otpLogin(Model model) {
        model.addAttribute("otpForm", new OtpForm());
        return "login-otp";
    }

    @PostMapping("/login/otp")
    public String verifyOtp(@Valid @ModelAttribute("otpForm") OtpForm otpForm,
                            BindingResult result,
                            Authentication authentication) {

        if (result.hasErrors()) {
            return "login-otp";
        } else if (!userService.verifyLoginOtp(otpForm.getOtp(), authentication.getName())) {
            result.rejectValue("otp", "token.otp.invalid");
            return "login-otp";
        } else {
            final UsernamePasswordAuthenticationToken fullAuthenticated =
                    new UsernamePasswordAuthenticationToken(
                            authentication.getPrincipal(),
                            authentication.getCredentials(),
                            userDetailsService.loadUserByUsername(authentication.getName())
                                    .getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(fullAuthenticated);

            return "redirect:/my-accounts";
        }
    }

    @ExceptionHandler(AccountLockedException.class)
    public String handleAccountLockedException(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?locked";
    }
}
