package pluralsight.m2.repository;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FraudRepository {

    private final Map<String, Boolean> fraudSuspected = new HashMap<>();

    public void markFraudSuspected(final String accountCode) {
        fraudSuspected.put(accountCode, true);
    }

    public boolean isFraudSuspected(final String accountCode) {
        return fraudSuspected.getOrDefault(accountCode, false);
    }
}
