package pluralsight.m13.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pluralsight.m13.domain.Account;
import pluralsight.m13.model.TransferModel;
import pluralsight.m13.repository.AccountRepository;
import pluralsight.m13.repository.TestDataFactory;
import pluralsight.m13.security.Roles;
import pluralsight.m13.util.AllowedRolesArgumentProvider;
import pluralsight.m13.util.DoubleArgumentsPerRole;
import pluralsight.m13.util.TestAccountBuilder;
import pluralsight.m13.util.AllowedRoles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountsServiceAccessControlTests {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT, Roles.CUSTOMER, Roles.HUMAN_RESOURCES})
    public void canViewAnyAccountThatBelongsToUser(
            Authentication authentication,
            boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        accountRepository.save(TestAccountBuilder.testAccountBuilder().build());

        final Throwable throwable = Assertions.catchThrowable(
                () -> accountsService.getAccountByCode("code"));

        if (permitted) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable).isNotNull();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT})
    public void canViewAnyAccountThatBelongsToAnyUserWithViewAccountsAuthority(
            Authentication authentication,
            boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        accountRepository.save(TestAccountBuilder.testAccountBuilder()
                .accountCode("code")
                .username("other")
                .build());

        final Throwable throwable = Assertions.catchThrowable(
                () -> accountsService.getAccountByCode("code"));

        if (permitted) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable).isNotNull();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER, Roles.CUSTOMER_SERVICE,
            Roles.SENIOR_VICE_PRESIDENT})
    public void verifyAnyAccountViewAccountsAuthority(Authentication authentication,
                                                      boolean anyAccount) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final Account userAccount1 = TestDataFactory.generateAccount("user", 1);
        final Account userAccount2 = TestDataFactory.generateAccount("user", 2);
        final Account otherAccount1 = TestDataFactory.generateAccount("other", 3);
        final Account otherAccount2 = TestDataFactory.generateAccount("other", 4);

        final Set<Account> allAccounts = Set.of(
                userAccount1,
                userAccount2,
                otherAccount1,
                otherAccount2
        );

        accountRepository.saveAll(allAccounts);

        final List<Account> found = accountsService.findAllAccounts();

        if (anyAccount) {
            assertThat(found)
                    .containsExactlyElementsOf(allAccounts);
        } else {
            assertThat(found)
                    .containsExactlyInAnyOrder(userAccount1, userAccount2);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT})
    @DoubleArgumentsPerRole({0, 1, 499.99})
    public void onlyAuthorizedUsersShouldPerformTransfers(Authentication authentication,
                                                          double transferAmount,
                                                          boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        accountRepository.save(TestAccountBuilder.testAccountBuilder()
                .accountCode("from")
                .build());

        accountRepository.save(TestAccountBuilder.testAccountBuilder()
                .accountCode("to")
                .build());

        final Throwable throwable = Assertions.catchThrowable(
                () -> accountsService.transfer(TransferModel.builder()
                        .fromAccountCode("from")
                        .toAccountCode("to")
                        .amount(BigDecimal.valueOf(transferAmount))
                        .build()));

        if (permitted) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable).isNotNull();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER, Roles.SENIOR_VICE_PRESIDENT})
    @DoubleArgumentsPerRole({500, 500.01, Double.MAX_VALUE})
    public void onlyAuthorizedUsersShouldPerformLargeTransfers(Authentication authentication,
                                                               double transferAmount,
                                                               boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        accountRepository.save(TestAccountBuilder.testAccountBuilder()
                .accountCode("from")
                .build());
        accountRepository.save(TestAccountBuilder.testAccountBuilder()
                .accountCode("to")
                .build());

        final Throwable throwable = Assertions.catchThrowable(
                () -> accountsService.transfer(TransferModel.builder()
                        .fromAccountCode("from")
                        .toAccountCode("to")
                        .amount(BigDecimal.valueOf(transferAmount))
                        .build()));

        if (permitted) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable).isNotNull();
        }
    }
}