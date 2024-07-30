package pluralsight.m8.repository;

import pluralsight.m8.domain.Account;

public interface CustomAccountRepository {
    Account getAccountByAccountCode(String accountCode);
}

