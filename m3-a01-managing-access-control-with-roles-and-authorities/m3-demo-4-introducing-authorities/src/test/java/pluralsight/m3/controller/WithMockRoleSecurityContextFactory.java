package pluralsight.m3.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pluralsight.m3.security.Roles;

public class WithMockRoleSecurityContextFactory implements WithSecurityContextFactory<WithMockRole> {
    @Override public SecurityContext createSecurityContext(final WithMockRole annotation) {
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        final Roles role = annotation.value();
        context.setAuthentication(TestUtils.createTestAuthentication(role));
        return context;
    }
}
