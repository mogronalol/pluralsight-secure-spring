package pluralsight.m3.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Roles {
    CUSTOMER,
    CUSTOMER_SERVICE(Authorities.VIEW_ACCOUNTS),
    CUSTOMER_SERVICE_MANAGER(Authorities.TRANSFERS, Authorities.VIEW_ACCOUNTS);

    private final Set<Authorities> authorities;

    Roles(final Authorities... authorities) {
        this.authorities = Arrays.stream(authorities).collect(Collectors.toSet());
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Stream.concat(
                        authorities.stream().map(a -> new SimpleGrantedAuthority(a.name())),
                        Stream.of(new SimpleGrantedAuthority(getGrantedAuthorityName())))
                .collect(Collectors.toSet());
    }

    public String getGrantedAuthorityName() {
        return "ROLE_" + this.name();
    }
}
