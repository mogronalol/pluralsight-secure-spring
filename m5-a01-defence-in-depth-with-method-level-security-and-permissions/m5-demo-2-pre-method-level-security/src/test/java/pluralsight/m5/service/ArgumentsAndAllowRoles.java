package pluralsight.m5.service;

import pluralsight.m5.security.Roles;

public @interface ArgumentsAndAllowRoles {
    String[] arguments();
    Roles[] allowedRoles();
}
