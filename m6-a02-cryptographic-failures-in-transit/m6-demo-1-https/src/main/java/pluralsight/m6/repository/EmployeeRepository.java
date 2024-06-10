package pluralsight.m6.repository;

import org.springframework.stereotype.Component;
import pluralsight.m6.domain.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EmployeeRepository {

    private final List<Employee> employees = new ArrayList<>();

    public void save(final Employee employee) {
        employees.add(employee);
    }

    public void deleteAll() {
        employees.clear();
    }

    public List<Employee> findAllEmployees() {
        return employees;
    }

    public Employee getEmployeeById(final UUID employeeId) {
        return employees
                .stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
