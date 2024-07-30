package pluralsight.m15.util;

import org.springframework.security.test.context.support.WithSecurityContext;
import pluralsight.m15.security.Roles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockRoleSecurityContextFactory.class)
public @interface WithMockRole {
    Roles value();
}
