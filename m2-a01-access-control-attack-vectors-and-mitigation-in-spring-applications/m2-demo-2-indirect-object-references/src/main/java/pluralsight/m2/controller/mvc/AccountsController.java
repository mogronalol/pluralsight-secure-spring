package pluralsight.m2.controller.mvc;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pluralsight.m2.repository.AccountRepository;

@Controller
public class AccountsController {
    private final AccountRepository accountRepository;

    public AccountsController(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/my-accounts")
    public String accounts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("accounts",
                accountRepository.getAccountForUser(userDetails.getUsername()));
        model.addAttribute("username", userDetails.getUsername());
        return "my-accounts";
    }

    @GetMapping("/accounts/{accountIndex}/transactions")
//    @GetMapping("/accounts/{accountCode}/transactions")
    public String transactions(@AuthenticationPrincipal final UserDetails userDetails,
                               final Model model,
                               @PathVariable("accountIndex") int accountIndex) {
//                               @PathVariable("accountCode") String accountCode) {
        model.addAttribute("account",
                accountRepository.getAccountForUser(userDetails.getUsername())
                        .get(accountIndex));
//        model.addAttribute("account", accountRepository.getAccountByCode(accountCode));
        model.addAttribute("username", userDetails.getUsername());
        return "transactions";
    }
}
