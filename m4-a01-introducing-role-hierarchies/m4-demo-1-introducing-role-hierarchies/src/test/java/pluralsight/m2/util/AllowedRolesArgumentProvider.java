package pluralsight.m2.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import pluralsight.m2.security.Roles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static pluralsight.m2.util.Utils.createTestAuthentication;

public class AllowedRolesArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        final AllowedRoles allowedRolesAnnotation =
                context.getRequiredTestMethod().getAnnotation(AllowedRoles.class);

        final Map<Roles, Set<String>> allowedPerRole =
                Arrays.stream(allowedRolesAnnotation.allowed())
                        .collect(toMap(AllowedRole::role, a -> Set.of(a.visibleResourceIds())));

        return Arrays.stream(Roles.values())
                .map(r -> {
                    if (allowedPerRole.containsKey(r)) {
                        final Set<String> canSee = allowedPerRole.get(r);

                        final Set<String> cannotSee =
                                Set.of(allowedRolesAnnotation.allResourceIds())
                                .stream()
                                .filter(Predicate.not(canSee::contains))
                                .collect(Collectors.toSet());

                        return Arguments.of(
                                createTestAuthentication(r),
                                canSee,
                                cannotSee,
                                true
                        );
                    } else {
                        return Arguments.of(
                                createTestAuthentication(r),
                                new HashSet<>(),
                                new HashSet<>(),
                                false
                        );
                    }
                });
    }
}