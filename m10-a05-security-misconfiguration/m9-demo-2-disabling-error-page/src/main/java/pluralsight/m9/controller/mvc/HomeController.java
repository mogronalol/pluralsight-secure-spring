package pluralsight.m9.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/error-example")
    public String redirectRootToAccounts() {
        throw new RuntimeException("Example error");
    }

    @RequestMapping("/")
    public String home() {
        return "home";
    }
}
