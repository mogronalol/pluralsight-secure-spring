package pluralsight.m5.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import pluralsight.m5.security.Roles;

import java.util.Arrays;
import java.util.stream.Stream;

import static pluralsight.m5.util.Utils.createTestAuthentication;

public class RoleBasedArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        AllowedRoles allowedRolesAnnotation =
                context.getRequiredTestMethod().getAnnotation(AllowedRoles.class);
        final Roles[] allowedRoles = allowedRolesAnnotation == null ? new Roles[]{} :
                allowedRolesAnnotation.value();

        return Arrays.stream(Roles.values())
                .map(role -> Arguments.of(createTestAuthentication(role),
                        Arrays.asList(allowedRoles).contains(role)));
    }
}