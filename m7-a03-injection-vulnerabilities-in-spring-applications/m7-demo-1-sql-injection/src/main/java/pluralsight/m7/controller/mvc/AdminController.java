package pluralsight.m7.controller.mvc;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pluralsight.m7.service.AccountsService;
import pluralsight.m7.model.TransferModel;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AccountsService accountsService;

    public AdminController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @GetMapping("/transfer")
    public String showTransferForm(Model model) {
        model.addAttribute("accounts", accountsService.findAllAccounts());
        model.addAttribute("transfer", new TransferModel());
        return "transfer";
    }

    @GetMapping("/accounts/{accountCode}")
    public String transactions(final Model model,
                               @PathVariable("accountCode") String accountCode) {

        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute("account", accountsService.getAccountByCode(accountCode));
        return "transactions";
    }

    @GetMapping("/accounts")
    public String accounts(final Model model) {

        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute("accounts", accountsService.findAllAccounts());
        return "accounts";
    }

    @PostMapping("/transfer")
    public String processTransfer(@ModelAttribute TransferModel transfer,
                                  final RedirectAttributes redirectAttributes) {

        accountsService.transfer(transfer);
        redirectAttributes.addFlashAttribute("completed", transfer);
        return "redirect:/admin/transfer";
    }
}

