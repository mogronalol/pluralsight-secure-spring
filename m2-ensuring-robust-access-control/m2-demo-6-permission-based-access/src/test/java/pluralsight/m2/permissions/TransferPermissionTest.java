package pluralsight.m2.permissions;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pluralsight.m2.model.TransferModel;
import pluralsight.m2.security.Authorities;
import pluralsight.m2.security.BankingPermissionEvaluator;
import pluralsight.m2.security.Permissions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferPermissionTest {

    private final BankingPermissionEvaluator bankingPermissionEvaluator = new BankingPermissionEvaluator();

    @TestFactory
    Stream<DynamicTest> generateTransferPermissionTests() {

        final List<Authorities> lowTransferAuthorities = List.of(Authorities.TRANSFERS);
        final List<Authorities> highTransferAuthorities = List.of(Authorities.TRANSFERS, Authorities.LARGE_TRANSFERS);
        final Set<Authorities> doesNotGrantTransfer = allAuthoritiesExcluding(Authorities.TRANSFERS);
        final List<String> lowTransfer = List.of("1", "500", "999.99");
        final List<String> highTransfer = List.of("1000", "1000000");

        List<DynamicTest> dynamicTests = new ArrayList<>();

        for (String amount : lowTransfer) {
            dynamicTests.add(verifyTransferPermission(new BigDecimal(amount), lowTransferAuthorities, true));
            dynamicTests.add(verifyTransferPermission(new BigDecimal(amount), highTransferAuthorities, true));
            for (Authorities authorities : doesNotGrantTransfer) {
                dynamicTests.add(verifyTransferPermission(new BigDecimal(amount), List.of(authorities), false));
            }
        }

        for (String amount : highTransfer) {
            dynamicTests.add(verifyTransferPermission(new BigDecimal(amount), lowTransferAuthorities, false));
            dynamicTests.add(verifyTransferPermission(new BigDecimal(amount), highTransferAuthorities, true));
            for (Authorities authorities : doesNotGrantTransfer) {
                dynamicTests.add(verifyTransferPermission(new BigDecimal(amount), List.of(authorities), false));
            }
        }

        return dynamicTests.stream();
    }

    private static Set<Authorities> allAuthoritiesExcluding(final Authorities ... values) {
        final Set<Authorities> excludedAsSet = Stream.of(values).collect(Collectors.toSet());
        return Arrays.stream(Authorities.values())
                .filter(a -> !excludedAsSet.contains(a))
                .collect(Collectors.toSet());
    }

    private DynamicTest verifyTransferPermission(BigDecimal transferSize, List<Authorities> authorities, final boolean permitted) {
        return DynamicTest.dynamicTest(String.format("transferSize=%s, authorities=%s, permitted=%s", transferSize, authorities, permitted), () -> {
            final Authentication authentication = createAuthenticationWithAuthorities(authorities);
            final Object targetDomainObject = new TransferModel("from", "to", transferSize);
            assertThat(bankingPermissionEvaluator.hasPermission(authentication, targetDomainObject, Permissions.EXECUTE))
                    .isEqualTo(permitted);
        });
    }

    private static Authentication createAuthenticationWithAuthorities(final List<Authorities> authorities) {
        final Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(a -> new SimpleGrantedAuthority(a.name()))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken("username", new Object(), simpleGrantedAuthorities);
    }
}