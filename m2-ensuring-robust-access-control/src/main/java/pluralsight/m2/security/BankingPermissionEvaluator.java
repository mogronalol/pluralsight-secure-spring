package pluralsight.m2.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pluralsight.m2.model.TransferModel;
import pluralsight.m2.service.AccountsService;

import java.io.Serializable;
import java.math.BigDecimal;

import static pluralsight.m2.security.Authorities.VIEW_FLAGGED_ACCOUNT;

@Component
public class BankingPermissionEvaluator implements PermissionEvaluator {
    private final AccountsService accountsService;

    public BankingPermissionEvaluator(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    private static boolean hasAuthority(final Authentication auth, final Authorities authority) {
        return auth.getAuthorities()
                .stream().anyMatch(a -> new SimpleGrantedAuthority(authority.name()).equals(a));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return hasPermission(authentication, targetDomainObject, Permissions.valueOf(permission.toString()));
    }


    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Permissions permission) {
        if (permission.equals(Permissions.TRANSFER) &&
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
    public boolean hasPermission(final Authentication authentication, final Serializable targetId, final String targetType, final Object permission) {
        return hasPermissionOnEntity(authentication, targetId.toString(), EntityTypes.valueOf(targetType.toUpperCase()), Permissions.valueOf(permission.toString().toUpperCase()));
    }

    private boolean hasPermissionOnEntity(final Authentication authentication, final String entityId, final EntityTypes entityType, final Permissions permission) {

        if (entityType.equals(EntityTypes.ACCOUNT)) {
            if (permission.equals(Permissions.VIEW)) {

                if (hasAuthority(authentication, Authorities.VIEW_ANY_ACCOUNT)) {
                    return true;
                }

                if (isAFraudAnalystAndAccountSuspectedOfFraud(authentication, entityId)) {
                    return true;
                }


                return accountBelongsToUser(authentication, entityId);
            }
        }

        return false;
    }

    private boolean accountBelongsToUser(final Authentication authentication, final String entityId) {
        return accountsService.getAccountByCode(entityId).getUsername().equals(authentication.getName());
    }

    private boolean isAFraudAnalystAndAccountSuspectedOfFraud(final Authentication authentication, final String accountCode) {
        return hasAuthority(authentication, VIEW_FLAGGED_ACCOUNT) && accountsService.isFraudSuspectedAccount(accountCode);
    }
}
