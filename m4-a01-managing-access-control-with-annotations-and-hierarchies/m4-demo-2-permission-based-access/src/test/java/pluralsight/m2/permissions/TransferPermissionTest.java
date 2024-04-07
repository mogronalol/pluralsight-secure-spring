package pluralsight.m2.permissions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.security.core.Authentication;
import pluralsight.m2.model.TransferModel;
import pluralsight.m2.security.BankingPermissionEvaluator;
import pluralsight.m2.security.Permissions;
import pluralsight.m2.security.Roles;
import pluralsight.m2.util.AllowedRoles;
import pluralsight.m2.util.TransactionAmounts;
import pluralsight.m2.util.TransactionPermissionTestArgumentProvider;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferPermissionTest {

    private final BankingPermissionEvaluator bankingPermissionEvaluator =
            new BankingPermissionEvaluator();

    @ParameterizedTest
    @ArgumentsSource(TransactionPermissionTestArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER})
    @TransactionAmounts({"1", "100", "99.99"})
    public void verifyLowTransferRoles(final Authentication authentication,
                                       final boolean permitted, final BigDecimal amount) {

        final TransferModel transferModel = TransferModel.builder().amount(amount).build();

        assertThat(bankingPermissionEvaluator.hasPermission(authentication, transferModel,
                Permissions.EXECUTE))
                .isEqualTo(permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(TransactionPermissionTestArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER})
    @TransactionAmounts({"1000", "5000", "1000000"})
    public void verifyLargeTransferRoles(final Authentication authentication,
                                         final boolean permitted, final BigDecimal amount) {

        final TransferModel transferModel = TransferModel.builder().amount(amount).build();

        assertThat(bankingPermissionEvaluator.hasPermission(authentication, transferModel,
                Permissions.EXECUTE))
                .isEqualTo(permitted);
    }
}