package pluralsight.m2.util;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pluralsight.m2.security.Roles;

import static pluralsight.m2.util.Utils.createTestAuthentication;

public class WithMockRoleSecurityContextFactory implements WithSecurityContextFactory<WithMockRole> {

    @Override
    public SecurityContext createSecurityContext(WithMockRole withMockRole) {
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        final Roles role = withMockRole.value();
        context.setAuthentication(createTestAuthentication(role));
        return context;
    }
}