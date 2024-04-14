package pluralsight.m2.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.security.core.Authentication;
import pluralsight.m2.config.SecurityConfig;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static pluralsight.m2.util.Utils.enrichWithRoleHierarchy;

public class TransactionPermissionTestArgumentProvider extends RoleBasedArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        final Stream<? extends Arguments> arguments = super.provideArguments(context);

        final TransactionAmounts annotation =
                context.getRequiredTestMethod().getAnnotation(TransactionAmounts.class);

        if (annotation == null || annotation.value() == null ||
                annotation.value().length == 0) {
            throw new IllegalArgumentException("Must provide at least one transaction amount");
        }

        final String[] transactionAmounts = annotation.value();

        return arguments
                .flatMap(a -> Stream.of(transactionAmounts).map(amount -> Arguments.of(
                        enrichWithRoleHierarchy((Authentication) a.get()[0],
                                new SecurityConfig().roleHierarchy()), a.get()[1],
                        new BigDecimal(amount))));
    }
}