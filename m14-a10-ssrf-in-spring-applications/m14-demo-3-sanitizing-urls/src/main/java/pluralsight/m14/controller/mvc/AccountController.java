package pluralsight.m14.controller.mvc;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import pluralsight.m14.domain.Account;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final RestTemplate restTemplate;

    @GetMapping("/")
    public String redirectRootToAccounts(Model model,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         @Pattern(regexp = "^[a-zA-Z0-9]*$")
                                             @RequestParam(required = false) String accountType) {

        String url = "http://internal-service.com" +
                           "/accounts?" +
                           "username=" + userDetails.getUsername();

        if (accountType != null) {
            url += "&accountType=" + accountType.toLowerCase();
        }

        final Account[] accounts = restTemplate.getForObject(url,
                Account[].class);

        model.addAttribute("accounts", accounts);
        return "accounts";
    }
}
