package pluralsight.m15.controller.mvc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import pluralsight.m15.domain.Employee;
import pluralsight.m15.security.MaskedToString;
import pluralsight.m15.service.EmployeesService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeesService employeesService;

    @GetMapping
    public String getEmployees(Model model) {
        List<Employee> employees = employeesService.findAllEmployees();
        model.addAttribute("employees", employees);
        return "employees";
    }

    @GetMapping("/{employeeId}")
    public String getEmployee(@ModelAttribute EmployeeId employeeId, Model model) {
        Employee employee = employeesService.getEmployeeById(employeeId);
        model.addAttribute("employee", employee);
        return "employee"; // Path to Thymeleaf template for employee details
    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    @Setter
    public static class EmployeeId extends MaskedToString {
        private UUID employeeId;
    }
}