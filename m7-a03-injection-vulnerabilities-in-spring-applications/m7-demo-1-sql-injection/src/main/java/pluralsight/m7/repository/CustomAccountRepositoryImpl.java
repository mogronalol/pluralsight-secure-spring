package pluralsight.m7.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pluralsight.m7.domain.Account;

public class CustomAccountRepositoryImpl
        implements CustomAccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Account getAccountByAccountCode(final String accountCode) {
        final String query =
                "SELECT * FROM Account WHERE account_code = '" + accountCode + "'";
        return (Account) entityManager.createNativeQuery(query, Account.class)
                .getSingleResult();
    }
}
