package pluralsight.m12.repository;

import org.springframework.stereotype.Component;
import pluralsight.m12.domain.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class UserRepository {
    private Map<String, User> users = new HashMap<>();

    public void saveUser(User user) {
        users.put(user.getUsername(), user);
    }

    public Optional<User> getUser(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public void deleteAll() {
        users.clear();
    }
}

