package pluralsight.m3.controller;

import pluralsight.m3.security.Roles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedRole {
    Roles role();
    String[] visibleResourceIds() default {};
}
