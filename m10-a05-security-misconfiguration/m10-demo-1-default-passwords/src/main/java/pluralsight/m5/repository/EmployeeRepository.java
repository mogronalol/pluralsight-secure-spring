package pluralsight.m5.repository;

import org.springframework.stereotype.Component;
import pluralsight.m5.domain.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EmployeeRepository implements GenericRepository<Employee, UUID> {

    private final List<Employee> employees = new ArrayList<>();

    @Override
    public void save(final Employee employee) {
        employees.add(employee);
    }

    @Override
    public void deleteAll() {
        employees.clear();
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(employees);
    }

    @Override
    public Employee findById(final UUID employeeId) {
        return employees.stream()
                        .filter(e -> e.getEmployeeId().equals(employeeId))
                        .findFirst()
                .orElseThrow(() -> new RuntimeException("Not found"));
    }
}
