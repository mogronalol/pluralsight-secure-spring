package pluralsight.m8.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pluralsight.m8.domain.Account;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{//},
    // CustomAccountRepository {
    List<Account> findByUsername(String username);
    Account getAccountByAccountCode(String accountCode);
}
