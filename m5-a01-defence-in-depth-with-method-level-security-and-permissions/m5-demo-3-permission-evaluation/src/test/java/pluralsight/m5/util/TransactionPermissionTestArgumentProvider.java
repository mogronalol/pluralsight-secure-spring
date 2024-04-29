package pluralsight.m5.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.util.stream.Stream;

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
                .flatMap(a -> Stream.of(transactionAmounts)
                        .map(amount -> Arguments.of(a.get()[0], a.get()[1],
                                new BigDecimal(amount))));
    }
}