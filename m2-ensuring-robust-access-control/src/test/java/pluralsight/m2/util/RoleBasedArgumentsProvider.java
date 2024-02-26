package pluralsight.m2.controller.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import pluralsight.m2.security.Roles;

import java.util.Arrays;
import java.util.stream.Stream;

public class RoleBasedArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        AllowedRoles allowedRolesAnnotation = context.getRequiredTestMethod().getAnnotation(AllowedRoles.class);
        Roles[] allowedRoles = allowedRolesAnnotation.value();

        return Arrays.stream(Roles.values())
                .map(role -> Arguments.of(role, Arrays.asList(allowedRoles).contains(role)));
    }
}