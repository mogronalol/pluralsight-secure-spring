package pluralsight.m13.repository;

import org.springframework.stereotype.Component;
import pluralsight.m13.controller.mvc.EmployeeController;
import pluralsight.m13.domain.Employee;

import java.util.ArrayList;
import java.util.List;

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

    public Employee getEmployeeById(final EmployeeController.EmployeeId employeeId) {
        return employees
                .stream()
                .filter(a -> a.getEmployeeId().equals(employeeId.getEmployeeId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
