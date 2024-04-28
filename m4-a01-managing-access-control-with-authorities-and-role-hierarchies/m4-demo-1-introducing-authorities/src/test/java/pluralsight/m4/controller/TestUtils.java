package pluralsight.m4.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import pluralsight.m4.security.Roles;

public class TestUtils {
    public static Authentication createTestAuthentication(final Roles role) {
        final User user = new User("username", "password", role.getGrantedAuthorities());
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(),
                role.getGrantedAuthorities()){
            @Override public String toString() {
                return String.format("{Role=%s, Authorities=%s}", role.name(),
                        role.getGrantedAuthorities());
            }
        };
    }
}
