package pluralsight.m5.controller.mvc;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pluralsight.m5.security.Roles;

import java.util.Set;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRootToAccounts(
            @AuthenticationPrincipal final UserDetails userDetails) {

        final Set<String> admins = Set.of("ROLE_" + Roles.CUSTOMER_SERVICE.name(),
                "ROLE_" + Roles.CUSTOMER_SERVICE_MANAGER.name());

        if (userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(admins::contains)) {
            return "redirect:/admin/accounts";
        }

        return "redirect:/my-accounts";
    }
}
