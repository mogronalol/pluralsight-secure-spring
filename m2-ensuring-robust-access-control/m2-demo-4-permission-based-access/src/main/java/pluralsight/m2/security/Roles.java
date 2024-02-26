package pluralsight.m2.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static pluralsight.m2.security.Authorities.*;

public enum Roles {
    CUSTOMER,
    CUSTOMER_SERVICE(TRANSFERS, VIEW_ANY_ACCOUNT),
    CUSTOMER_SERVICE_MANAGER(TRANSFERS, LARGE_TRANSFERS, VIEW_ANY_ACCOUNT),
    FRAUD_ANALYST(VIEW_FLAGGED_ACCOUNT),
    OTHER();

    private final Set<Authorities> authorities;

    Roles(Authorities... authorities) {
        this.authorities = Arrays.stream(authorities).collect(toSet());
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Stream.concat(authorities.stream()
                        .map(a -> new SimpleGrantedAuthority(a.name())), Stream.of(new SimpleGrantedAuthority(grantedAuthorityName())))
                .collect(toSet());
    }

    public String grantedAuthorityName() {
        return "ROLE_" + this.name();
    }
}
