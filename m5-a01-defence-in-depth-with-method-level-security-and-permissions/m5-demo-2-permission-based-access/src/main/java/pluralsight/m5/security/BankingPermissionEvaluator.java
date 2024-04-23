package pluralsight.m5.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pluralsight.m5.model.TransferModel;

import java.io.Serializable;
import java.math.BigDecimal;

@Component
public class BankingPermissionEvaluator implements PermissionEvaluator {

    private static boolean hasAuthority(final Authentication auth,
                                        final Authorities authority) {
        return auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> authority.name().equals(a));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject,
                                 Object permission) {
        if (Permissions.valueOf(permission.toString()).equals(Permissions.EXECUTE) &&
            targetDomainObject instanceof final TransferModel transferModel &&
            hasAuthority(authentication, Authorities.TRANSFERS)) {

            if (transferModel.getAmount().compareTo(BigDecimal.valueOf(1000)) < 0) {
                return true;
            }

            return hasAuthority(authentication, Authorities.LARGE_TRANSFERS);
        }

        return false;
    }

    @Override
    public boolean hasPermission(final Authentication authentication,
                                 final Serializable targetId, final String targetType,
                                 final Object permission) {
        return false;
    }
}
