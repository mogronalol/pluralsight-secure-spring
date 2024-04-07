package pluralsight.m2.util;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pluralsight.m2.security.Roles;

import java.util.List;
import java.util.stream.Stream;

public class Utils {
    public static Authentication createTestAuthentication(final Roles role) {

        return createAuthentication(List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private static UsernamePasswordAuthenticationToken createAuthentication(
            final List<GrantedAuthority> grantedAuthorities) {

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

    public static Authentication enrichWithRoleHierarchy(final Authentication authentication,
                                                         final RoleHierarchy roleHierarchy) {

        final List<GrantedAuthority> authorities = Stream.concat(
                        authentication.getAuthorities().stream(),
                        roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities())
                                .stream())
                .toList();

        return createAuthentication(authorities);
    }
}
