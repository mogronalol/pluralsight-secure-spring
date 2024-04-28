package pluralsight.m5.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pluralsight.m5.security.Roles;

import java.util.Set;

public class Utils {
    public static Authentication createTestAuthentication(final Roles role) {

        return createAuthentication(Set.of(new SimpleGrantedAuthority(
                role.getGrantedAuthorityName())));
    }

    private static UsernamePasswordAuthenticationToken createAuthentication(
            final Set<SimpleGrantedAuthority> grantedAuthorities) {

        final UserDetails principal = new User("user", "password", grantedAuthorities);
        return new UsernamePasswordAuthenticationToken(principal, "password",
                grantedAuthorities) {
            @Override
            public String toString() {
                return String.format("{Authorities=%s}",
                        grantedAuthorities.stream().map(GrantedAuthority::getAuthority)
                                .toList());
            }
        };
    }
}
