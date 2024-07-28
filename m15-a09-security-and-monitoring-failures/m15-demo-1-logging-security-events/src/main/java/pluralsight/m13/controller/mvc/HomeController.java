package pluralsight.m13.controller.mvc;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pluralsight.m13.security.Roles;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRootToAccounts(
            @AuthenticationPrincipal final UserDetails userDetails) {

        final Set<Roles> usersRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.replaceAll("ROLE_", ""))
                .map(Roles::valueOf)
                .collect(Collectors.toSet());

        if (usersRoles.contains(Roles.CUSTOMER_SERVICE)
                || usersRoles.contains(Roles.CUSTOMER_SERVICE_MANAGER)) {
            return "redirect:/admin/accounts";
        } else if (usersRoles.contains(Roles.HUMAN_RESOURCES)) {
            return "redirect:/employees";
        } else if (usersRoles.contains(Roles.CUSTOMER)) {
            return "redirect:/my-accounts";
        }

        throw new AccessDeniedException("UnsupportedRole");
    }
}
