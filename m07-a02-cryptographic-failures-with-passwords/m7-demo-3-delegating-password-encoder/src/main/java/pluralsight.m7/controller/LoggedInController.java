package pluralsight.m7.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoggedInController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
}
