package pluralsight.m2.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pluralsight.m2.security.Roles;

import java.util.ArrayList;
import java.util.List;

public class WithMockRoleSecurityContextFactory implements WithSecurityContextFactory<WithMockRole> {

    @Override
    public SecurityContext createSecurityContext(WithMockRole withMockRole) {
        final SecurityContext context = SecurityContextHolder.createEmptyContext();

        final Roles role = withMockRole.value();
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>(role.getGrantedAuthorities());
        final UserDetails principal = new User("user", "password", grantedAuthorities);
        final Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", grantedAuthorities);

        context.setAuthentication(auth);
        return context;
    }
}