package pluralsight.m9.controller.mvc;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final Environment environment;

    @GetMapping("/error-example")
    public String redirectRootToAccounts() {
        throw new RuntimeException("Example error");
    }

    @RequestMapping("/")
    public String home() {
        return "home";
    }
}
