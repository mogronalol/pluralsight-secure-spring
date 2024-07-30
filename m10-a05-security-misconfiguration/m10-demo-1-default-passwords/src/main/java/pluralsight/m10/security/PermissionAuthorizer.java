package pluralsight.m10.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pluralsight.m10.domain.Account;
import pluralsight.m10.model.TransferModel;

import java.math.BigDecimal;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

@Component
@RequiredArgsConstructor
public class PermissionAuthorizer {

    private final RoleHierarchy roleHierarchy;

    private static UserDetails getUserDetails() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
    }

    public boolean canPerformTransfer(final TransferModel transferModel) {

        final Set<Authorities> userAuthorities = getUserAuthorities(getUserDetails());

        final boolean lowTransfer =
                transferModel.getAmount().compareTo(new BigDecimal(500)) < 0;

        if (lowTransfer) if (userAuthorities.contains(Authorities.TRANSFERS)) return true;
        return userAuthorities.contains(Authorities.LARGE_TRANSFERS);
    }

    public boolean getCanViewAccount(final Account account) {

        final UserDetails userDetails = getUserDetails();
        final Set<Authorities> userAuthorities = getUserAuthorities(userDetails);
        final String username = userDetails.getUsername();

        return account.getUsername().equals(username) ||
               userAuthorities.contains(Authorities.VIEW_ACCOUNTS);
    }

    private Set<Authorities> getUserAuthorities(final UserDetails userDetails) {
        return roleHierarchy.getReachableGrantedAuthorities(userDetails.getAuthorities())
                .stream()
                .filter(not(a -> a.getAuthority().startsWith("ROLE_")))
                .map(a -> Authorities.valueOf(a.getAuthority()))
                .collect(toSet());
    }
}
