package pluralsight.m5.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pluralsight.m5.domain.Account;
import pluralsight.m5.model.TransferModel;
import pluralsight.m5.repository.AccountRepository;
import pluralsight.m5.repository.TestDataFactory;
import pluralsight.m5.security.Roles;
import pluralsight.m5.util.AllowedRoleArguments;
import pluralsight.m5.util.AllowedRoles;
import pluralsight.m5.util.AllowedRolesArgumentProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

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

        accountRepository.save(Account.builder().accountCode("code").username("user").build());

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

        accountRepository.save(
                Account.builder().accountCode("code").username("other").build());

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
    @AllowedRoleArguments(doubleArguments = {0.01, 250, 499.99})
    public void onlyUsersWithTransfersOrLargeTransfersAuthorityCanPerformLowTransfers(
            Authentication authentication,
            boolean permitted,
            double transactionAmount
    ) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        accountRepository.save(Account.builder().accountCode("from").build());
        accountRepository.save(Account.builder().accountCode("to").build());

        final Throwable throwable =
                catchThrowable(() -> accountsService.transfer(TransferModel.builder()
                        .fromAccountCode("from")
                        .toAccountCode("to")
                        .amount(new BigDecimal(transactionAmount))
                        .build()));

        if(permitted) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable).isInstanceOf(AccessDeniedException.class);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT})
    @AllowedRoleArguments(doubleArguments = {500, 500.01, Double.MAX_VALUE})
    public void onlyUsersWithLargeTransfersAuthorityCanPerformLargeTransfers(
            Authentication authentication,
            boolean permitted,
            double transactionAmount
    ) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        accountRepository.save(Account.builder().accountCode("from").build());
        accountRepository.save(Account.builder().accountCode("to").build());

        final Throwable throwable =
                catchThrowable(() -> accountsService.transfer(TransferModel.builder()
                        .fromAccountCode("from")
                        .toAccountCode("to")
                        .amount(new BigDecimal(transactionAmount))
                        .build()));

        if(permitted) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable).isInstanceOf(AccessDeniedException.class);
        }
    }
}