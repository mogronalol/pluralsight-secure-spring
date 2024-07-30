package pluralsight.m10.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pluralsight.m10.repository.EmployeeRepository;
import pluralsight.m10.domain.Employee;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class  EmployeesService {
    private final EmployeeRepository employeeRepository;

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(final UUID employeeId) {
        return employeeRepository.findById(employeeId);
    }
}
