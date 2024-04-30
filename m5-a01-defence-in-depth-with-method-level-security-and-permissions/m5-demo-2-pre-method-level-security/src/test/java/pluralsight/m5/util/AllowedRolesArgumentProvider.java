package pluralsight.m5.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import pluralsight.m5.security.Roles;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static pluralsight.m5.util.Utils.createTestAuthentication;

public class AllowedRolesArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {

        final AllowedRoles allowedRolesAnnotations =
                context.getRequiredTestMethod().getAnnotation(AllowedRoles.class);

        final Set<Roles> allowedRoles =
                allowedRolesAnnotations == null || allowedRolesAnnotations.value() == null ?
                        Collections.emptySet() :
                        Arrays.stream(allowedRolesAnnotations.value()).collect(toSet());

        return Stream.of(Roles.values())
                .map(r -> Arguments.of(createTestAuthentication(r),
                        allowedRoles.contains(r)));
    }
}
