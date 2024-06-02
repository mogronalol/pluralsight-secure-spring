package pluralsight.m7.repository;

import pluralsight.m7.domain.Account;

public interface CustomAccountRepository {
    Account getAccountByAccountCode(String accountCode);
}

