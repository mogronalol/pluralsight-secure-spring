package pluralsight.m8.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String redirectRootToAccounts() {

        return "redirect:/admin/accounts";
    }
}