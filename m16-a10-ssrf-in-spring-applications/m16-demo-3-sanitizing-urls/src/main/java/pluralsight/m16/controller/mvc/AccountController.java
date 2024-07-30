package pluralsight.m16.controller.mvc;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import pluralsight.m16.domain.Account;
import pluralsight.m16.domain.AccountType;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final RestClient restClient;

    @GetMapping("/")
    public String fetchAccounts(Model model,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         @RequestParam(required = false)
                                AccountType accountType) {

        String url = "http://internal-service.com" +
                           "/accounts?" +
                           "username=" + userDetails.getUsername();

        if (accountType != null) {
            url += "&accountType=" + accountType;
        }

        final Account[] accounts = restClient.get().uri(url).
                retrieve()
                .body(Account[].class);

        model.addAttribute("accounts", accounts);
        return "accounts";
    }
}
