package pluralsight.m3.controller;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import pluralsight.m3.security.Roles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public class RoleBasedArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        final AllowedRoles allowedRolesAnnotation =
                context.getRequiredTestMethod().getAnnotation(AllowedRoles.class);

        final Map<Roles, String[]> allowedRoleResourceIds =
                Stream.of(allowedRolesAnnotation.allowed())
                        .collect(Collectors.toMap(AllowedRole::role,
                                AllowedRole::visibleResourceIds));

        return Stream.of(Roles.values())
                .map(r -> {
                    if (allowedRoleResourceIds.containsKey(r)) {
                        final Set<String> canBeSeen = Set.of(allowedRoleResourceIds.get(r));

                        final Set<String> cannotBeSeen = new HashSet<>(Arrays.asList(
                                allowedRolesAnnotation.allResourceIds()));

                        cannotBeSeen.removeAll(canBeSeen);

                        return Arguments.of(r, canBeSeen, cannotBeSeen, true);
                    }

                    return Arguments.of(r, emptySet(), emptySet(), false);
                });
    }
}