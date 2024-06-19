package pluralsight.m12.controller.mvc;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class LoginController {

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        final Object locked = session.getAttribute("locked");

        if (locked != null && locked.equals(true)) {
            model.addAttribute("locked", true);
            session.removeAttribute("locked");
        }

        final Object compromisedPassword = session.getAttribute("compromisedPassword");
        if (compromisedPassword != null && compromisedPassword.equals(true)) {
            model.addAttribute("compromisedPassword", true);
            session.removeAttribute("compromisedPassword");
        }

        return "login";
    }
}
