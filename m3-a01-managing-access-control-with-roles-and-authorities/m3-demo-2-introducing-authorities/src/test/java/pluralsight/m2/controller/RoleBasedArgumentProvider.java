package pluralsight.m2.controller;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import pluralsight.m2.security.Roles;

import java.util.Arrays;
import java.util.stream.Stream;

public class RoleBasedArgumentProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context)
            throws Exception {

        final AllowedRoles allowedRolesAnnotation =
                context.getRequiredTestMethod().getAnnotation(AllowedRoles.class);

        final Roles[] allowedRoles = allowedRolesAnnotation == null ? new Roles[]{} :
                allowedRolesAnnotation.value();

        return Arrays.stream(Roles.values())
                .map(r -> Arguments.of(r, Arrays.asList(allowedRoles).contains(r)));
    }
}
