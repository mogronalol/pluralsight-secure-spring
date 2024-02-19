package pluralsight.m2.permissions;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pluralsight.m2.domain.Account;
import pluralsight.m2.security.Authorities;
import pluralsight.m2.security.BankingPermissionEvaluator;
import pluralsight.m2.security.Permissions;
import pluralsight.m2.service.AccountsService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static pluralsight.m2.permissions.TestUtils.allAuthoritiesExcluding;
import static pluralsight.m2.permissions.TestUtils.createAuthenticationWithAuthorities;

@ExtendWith(MockitoExtension.class)
public class ViewAccountPermissionTest {

    @Mock
    private AccountsService accountsService;

    @InjectMocks
    private BankingPermissionEvaluator bankingPermissionEvaluator;

    @TestFactory
    Stream<DynamicTest> generateTransferPermissionTests() {

        final List<TestViewAccountParams> permitted = List.of(
                // Permitted if can view any account
                new TestViewAccountParams(true, true, Authorities.VIEW_ANY_ACCOUNT),
                new TestViewAccountParams(true, false, Authorities.VIEW_ANY_ACCOUNT),
                // Permitted if account is flagged and can view flagged accounts
                new TestViewAccountParams(true, true, Authorities.VIEW_FLAGGED_ACCOUNT),
                // Permitted if account is owned by user
                new TestViewAccountParams(false, false, Authorities.VIEW_ANY_ACCOUNT),
                new TestViewAccountParams(true, false),
                new TestViewAccountParams(false, false)
        );

        final Authorities[] authoritiesWithoutPermission = allAuthoritiesExcluding(Authorities.VIEW_ANY_ACCOUNT, Authorities.VIEW_ANY_ACCOUNT, Authorities.VIEW_FLAGGED_ACCOUNT);

        final List<TestViewAccountParams> notPermitted = List.of(
                // Not permitted if account is not flagged with view flagged account authority
                new TestViewAccountParams(false, true, Authorities.VIEW_FLAGGED_ACCOUNT),
                // Not permitted if owned by different user
                new TestViewAccountParams(true, true),
                new TestViewAccountParams(false, true),
                // Not permitted for any other role so long as usernames do not match
                new TestViewAccountParams(false, true, authoritiesWithoutPermission),
                new TestViewAccountParams(true, true, authoritiesWithoutPermission)
        );

        return Stream.concat(
                permitted.stream()
                        .map(p -> verifyViewAccountPermission(p, true)),
                notPermitted.stream()
                        .map(p -> verifyViewAccountPermission(p, false))
        );
    }

    private DynamicTest verifyViewAccountPermission(final TestViewAccountParams p, final boolean permitted) {
        return DynamicTest.dynamicTest(p.toString() + ", permitted=" + permitted, () -> {
            lenient().when(accountsService.isFraudSuspectedAccount("accountCode")).thenReturn(p.suspectedFraud());
            lenient().when(accountsService.getAccountByCode("accountCode")).thenReturn(Account.builder()
                    .username(p.differentUsername() ? "different" : "username")
                    .build());
            final boolean hasPermission = bankingPermissionEvaluator.hasPermission(createAuthenticationWithAuthorities(p.roles()), "accountCode", "ACCOUNT", Permissions.VIEW);
            assertThat(hasPermission).isEqualTo(permitted);
        });
    }

    public record TestViewAccountParams(boolean suspectedFraud, boolean differentUsername, List<Authorities> roles) {
        public TestViewAccountParams(boolean suspectedFraud, boolean differentUsername, Authorities... roles) {
            this(suspectedFraud, differentUsername, List.of(roles));
        }
    }
}