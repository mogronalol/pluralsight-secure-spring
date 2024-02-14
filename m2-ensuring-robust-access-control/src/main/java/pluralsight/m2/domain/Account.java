package pluralsight.m2.domain;

import java.util.List;

public record Account(String accountCode, int index, String userName, String displayName, double balance, List<Transaction> transactions) {

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
