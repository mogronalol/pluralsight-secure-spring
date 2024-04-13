package pluralsight.m2.controller.mvc;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pluralsight.m2.security.Roles;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRootToAccounts(
            @AuthenticationPrincipal final UserDetails userDetails) {

        final Set<String> roles =
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(
                                Collectors.toSet());

        if (roles.contains(Roles.CUSTOMER_SERVICE.grantedAuthorityName()) ||
                roles.contains(Roles.CUSTOMER_SERVICE_MANAGER.grantedAuthorityName())) {
            return "redirect:/admin/accounts";
        } else if (roles.contains(Roles.CUSTOMER.grantedAuthorityName())) {
            return "redirect:/my-accounts";
        }

        throw new AccessDeniedException("Not a customer or admin");
    }
}
