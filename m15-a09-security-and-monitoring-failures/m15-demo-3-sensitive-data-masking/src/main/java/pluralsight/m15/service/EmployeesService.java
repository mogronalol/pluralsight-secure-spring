package pluralsight.m15.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pluralsight.m15.controller.mvc.EmployeeController;
import pluralsight.m15.domain.Employee;
import pluralsight.m15.repository.EmployeeRepository;

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
