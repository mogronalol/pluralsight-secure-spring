package pluralsight.m6.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pluralsight.m6.domain.Employee;
import pluralsight.m6.repository.EmployeeRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeesService {
    private final EmployeeRepository employeeRepository;

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAllEmployees();
    }

    public Employee getEmployeeById(final UUID employeeId) {
        return employeeRepository.getEmployeeById(employeeId);
    }
}
