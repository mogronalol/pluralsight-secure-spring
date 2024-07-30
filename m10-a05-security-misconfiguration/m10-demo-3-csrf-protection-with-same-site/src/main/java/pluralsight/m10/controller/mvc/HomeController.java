package pluralsight.m10.controller.mvc;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pluralsight.m10.security.Roles;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRootToAccounts(@AuthenticationPrincipal UserDetails userDetails) {

        final Set<String> usersAuthorities = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(toSet());

        if (usersAuthorities.contains(Roles.CUSTOMER_SERVICE.getGrantedAuthorityName())
                || usersAuthorities.contains(
                Roles.CUSTOMER_SERVICE_MANAGER.getGrantedAuthorityName())) {
            return "redirect:/admin/accounts";
        } else if (usersAuthorities.contains(Roles.CUSTOMER.getGrantedAuthorityName())) {
            return "redirect:/my-accounts";
        }

        throw new AccessDeniedException("User not a customer or customer service");
    }
}
