package pluralsight.m13.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pluralsight.m13.controller.mvc.EmployeeController;
import pluralsight.m13.domain.Employee;
import pluralsight.m13.repository.EmployeeRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class  EmployeesService {
    private final EmployeeRepository employeeRepository;

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAllEmployees();
    }

    public Employee getEmployeeById(final EmployeeController.EmployeeId employeeId) {
        log.info("Getting employee by ID {}", employeeId);
        return employeeRepository.getEmployeeById(employeeId);
    }
}
