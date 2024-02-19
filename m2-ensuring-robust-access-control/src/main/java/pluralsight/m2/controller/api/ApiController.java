package pluralsight.m2.controller.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pluralsight.m2.service.AccountsService;

@Controller
@RequestMapping("/api")
public class ApiController {

    private final AccountsService accountsService;

    public ApiController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @GetMapping("/accounts/{accountCode}")
    @PreAuthorize("hasPermission(#accountCode, 'ACCOUNT', 'VIEW')")
    public String transactions(final Model model,
                               @PathVariable("accountCode") String accountCode) {
        model.addAttribute("account", accountsService.getAccountByCode(accountCode));
        return "transactions";
    }
}

