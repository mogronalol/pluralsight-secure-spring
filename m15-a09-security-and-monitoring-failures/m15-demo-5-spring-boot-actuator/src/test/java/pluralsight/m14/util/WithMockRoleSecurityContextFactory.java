package pluralsight.m14.util;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pluralsight.m14.security.Roles;

public class WithMockRoleSecurityContextFactory implements WithSecurityContextFactory<WithMockRole> {
    @Override public SecurityContext createSecurityContext(final WithMockRole annotation) {
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        final Roles role = annotation.value();
        context.setAuthentication(Utils.createTestAuthentication(role));
        return context;
    }
}
