package pluralsight.m14.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.dsl.StubServiceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pluralsight.m14.domain.Account;
import pluralsight.m14.domain.AccountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;

@Configuration
public class InternalServiceHoverflyStubConfig {
    public static final List<String> USER_NAMES =
            List.of("tom", "jack", "jane", "jill", "jason");
    private static final Random RANDOM = new Random(1);
    private static final List<String> ACCOUNT_TYPES = List.of("Current", "Credit", "Savings"
            , "Investment");

    public static Account generateAccount(final String username,
                                          final int accountIndex,
                                          final String type) {
        final AccountType[] accountTypes = AccountType.values();
        int randomIndex = RANDOM.nextInt(accountTypes.length);

        return Account.builder()
                .accountCode(generateAccountCode())
                .index(accountIndex)
                .username(username)
                .displayName(
                        accountTypes[randomIndex].getDisplayName() + " " + (accountIndex + 1))
                .balance(BigDecimal.valueOf(RANDOM.nextDouble()))
                .type(type)
                .build();
    }

    private static String generateAccountCode() {
        StringBuilder accountCode = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int digit = RANDOM.nextInt(10);
            accountCode.append(digit);
        }

        return accountCode.toString();
    }

    private static void simulateResponseForUser(final String username,
                                                StubServiceBuilder stubServiceBuilder) {

        for (var type : ACCOUNT_TYPES) {
            stubServiceBuilder.get("/accounts")
                    .queryParam("username", username)
                    .queryParam("accountType", type.toLowerCase())
                    .willReturn(success(
                            json(accountsForUsernameOfType(4, username,
                                    type))
                    ));
        }

        stubServiceBuilder.get("/accounts")
                .queryParam("username", username)
                .willReturn(success(
                        json(accountsForUsername(10, username))
                ));
    }

    private static List<Account> accountsForUsername(final int endExclusive,
                                                     final String username) {
        return IntStream.range(0, endExclusive)
                .mapToObj(i -> generateAccount(username, i,
                        ACCOUNT_TYPES.get(RANDOM.nextInt(3))))
                .toList();
    }

    private static List<Account> accountsForUsernameOfType(final int endExclusive,
                                                           final String username,
                                                           final String type) {
        return IntStream.range(0, endExclusive)
                .mapToObj(i -> generateAccount(username, i, type))
                .toList();
    }

    private static void simulateResponseForAdmin(
            final StubServiceBuilder stubServiceBuilder) {

        for (var accountType : ACCOUNT_TYPES) {

            final List<Account> allAccounts = USER_NAMES
                    .stream()
                    .flatMap(u -> accountsForUsernameOfType(2, u, accountType).stream())
                    .toList();

            for (var username : USER_NAMES) {

                stubServiceBuilder
                        .get("/accounts")
                        .queryParam("admin", true)
                        .queryParam("accountType", accountType.toLowerCase())
                        .queryParam("username", username)
                        .willReturn(success(
                                json(allAccounts)
                        ));

            }
        }

        for (var username : USER_NAMES) {
            stubServiceBuilder
                    .get("/accounts")
                    .queryParam("admin", true)
                    .queryParam("username", username)
                    .willReturn(success(
                            json(USER_NAMES
                                    .stream()
                                    .flatMap(username1 -> accountsForUsername(2,
                                            username1).stream())
                                    .toList())
                    ));
        }
    }

    @Bean(destroyMethod = "close")
    public Hoverfly hoverfly() {
        final Hoverfly hoverfly =
                new Hoverfly(HoverflyMode.SIMULATE);

        hoverfly.start();

        final StubServiceBuilder serviceBuilder = service("http://internal-service.com");

        for (var username : USER_NAMES) {
            simulateResponseForUser(username, serviceBuilder);
        }
        simulateResponseForAdmin(serviceBuilder);

        hoverfly.simulate(dsl(serviceBuilder));

        return hoverfly;
    }
}
