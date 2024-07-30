package pluralsight.m10.repository;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class UserRepository implements GenericRepository<UserDetails, String> {

    private final List<UserDetails> allUsers = new ArrayList<>();

    @Override
    public void save(final UserDetails account) {
        allUsers.add(account);
    }

    public void saveAll(final Set<UserDetails> users) {
        allUsers.addAll(users);
    }

    @Override
    public void deleteAll() {
        allUsers.clear();
    }

    @Override
    public List<UserDetails> findAll() {
        return new ArrayList<>(allUsers);
    }

    @Override
    public UserDetails findById(final String username) {
        return allUsers.stream()
                          .filter(a -> a.getUsername().equals(username))
                          .findFirst()
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public List<UserDetails> getAccountForUser(final String username) {
        return allUsers.stream().filter(a -> a.getUsername().equals(username)).toList();
    }
}
