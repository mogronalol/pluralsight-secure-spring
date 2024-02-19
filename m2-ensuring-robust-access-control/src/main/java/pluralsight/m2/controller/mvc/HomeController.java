package pluralsight.m2.controller.mvc;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRootToAccounts(@AuthenticationPrincipal final UserDetails userDetails) {
        return "redirect:/" + (userDetails.getUsername().equals("admin") ? "admin/accounts" : "my-accounts");
    }
}
