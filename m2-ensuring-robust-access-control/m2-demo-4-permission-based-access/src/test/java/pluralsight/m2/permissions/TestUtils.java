package pluralsight.m2.permissions;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pluralsight.m2.security.Authorities;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtils {
    public static UsernamePasswordAuthenticationToken createAuthenticationWithAuthorities(final List<Authorities> authorities) {
        final Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(a -> new SimpleGrantedAuthority(a.name()))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken("username", new Object(), simpleGrantedAuthorities);
    }

    public static Authorities[] allAuthoritiesExcluding(final Authorities... excluded) {
        final Set<Authorities> excludedAsSet = Stream.of(excluded).collect(Collectors.toSet());
        return Arrays.stream(Authorities.values())
                .filter(a -> !excludedAsSet.contains(a))
                .toArray(Authorities[]::new);
    }
}
