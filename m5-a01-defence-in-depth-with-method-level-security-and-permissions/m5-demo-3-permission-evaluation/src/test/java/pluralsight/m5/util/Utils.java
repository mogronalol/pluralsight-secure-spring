package pluralsight.m5.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pluralsight.m5.security.Roles;

public class Utils {
    public static Authentication createTestAuthentication(final Roles role) {
        final UserDetails principal =
                new User("user", "password", role.getGrantedAuthorities());
        return new UsernamePasswordAuthenticationToken(principal, "password",
                role.getGrantedAuthorities()) {
            @Override
            public String toString() {
                return String.format("{Role=%s, Authorities=%s}", role.name(),
                        role.getAuthorities());
            }
        };
    }

    public static boolean hasRole(Authentication authentication, Roles role) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals("ROLE_" + role.name()));
    }
}
