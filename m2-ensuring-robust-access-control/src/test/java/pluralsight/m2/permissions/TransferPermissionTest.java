package pluralsight.m2.permissions;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pluralsight.m2.model.TransferModel;
import pluralsight.m2.security.Authorities;
import pluralsight.m2.security.BankingPermissionEvaluator;
import pluralsight.m2.security.Permissions;
import pluralsight.m2.service.AccountsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static pluralsight.m2.permissions.TestUtils.allAuthoritiesExcluding;
import static pluralsight.m2.permissions.TestUtils.createAuthenticationWithAuthorities;

@ExtendWith(MockitoExtension.class)
public class TransferPermissionTest {

    @Mock
    private AccountsService accountsService;

    @InjectMocks
    private BankingPermissionEvaluator bankingPermissionEvaluator;



    @TestFactory
    Stream<DynamicTest> generateTransferPermissionTests() {

        final List<TestTransferParams> permitted = List.of(
                // Permitted if less than 1000 and has transfer authority
                new TestTransferParams(new BigDecimal("1"), Authorities.TRANSFERS),
                new TestTransferParams(new BigDecimal("999.99"), Authorities.TRANSFERS),
                // Permitted for any amount if has large transfer authority
                new TestTransferParams(new BigDecimal("1"), Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS),
                new TestTransferParams(new BigDecimal("999.99"), Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS),
                new TestTransferParams(new BigDecimal("1000"), Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS),
                new TestTransferParams(new BigDecimal("1001"), Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS),
                new TestTransferParams(new BigDecimal("100000"), Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS)
        );

        final Authorities[] allNotPermittedAuthorities = allAuthoritiesExcluding(Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS);

        final List<TestTransferParams> notPermitted = List.of(
                // Not permitted if 1000 or higher and only has transfer permission
                new TestTransferParams(new BigDecimal("1000"), Authorities.TRANSFERS),
                new TestTransferParams(new BigDecimal("1001"), Authorities.TRANSFERS),
                new TestTransferParams(new BigDecimal("100000"), Authorities.TRANSFERS),
                // Not permitted for any authority which is not TRANSFER or LARGE_TRANSFER
                new TestTransferParams(new BigDecimal("1"), allNotPermittedAuthorities),
                new TestTransferParams(new BigDecimal("999.99"), allNotPermittedAuthorities),
                new TestTransferParams(new BigDecimal("1000"), allNotPermittedAuthorities),
                new TestTransferParams(new BigDecimal("1001"), allNotPermittedAuthorities),
                new TestTransferParams(new BigDecimal("100000"), allNotPermittedAuthorities)
        );

        return Stream.concat(
                permitted.stream()
                        .map(p -> verifyTransferPermission(p, true)),
                notPermitted.stream()
                        .map(p -> verifyTransferPermission(p, false))
        );
    }

    private DynamicTest verifyTransferPermission(final TestTransferParams p, final boolean permitted) {
        return DynamicTest.dynamicTest(p.toString() + ", permitted=" + permitted, () -> {
            final boolean hasPermission = bankingPermissionEvaluator.hasPermission(createAuthenticationWithAuthorities(p.authorities()), new TransferModel("from", "to", p.transferSize()), Permissions.TRANSFER);
            assertThat(hasPermission).isEqualTo(permitted);
        });
    }

    public record TestTransferParams(BigDecimal transferSize, List<Authorities> authorities) {
        public TestTransferParams(BigDecimal transferSize, Authorities... authorities) {
            this(transferSize, List.of(authorities));
        }
    }
}