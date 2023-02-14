package ru.trae.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.trae.backend.dto.EmployeeDto;
import ru.trae.backend.dto.mapper.EmployeeDtoMapper;
import ru.trae.backend.entity.user.Employee;
import ru.trae.backend.exceptionhandler.exception.EmployeeException;
import ru.trae.backend.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeDtoMapper employeeDtoMapper;

    public void saveNewEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByPinCode(dto.pinCode())) return;

        Employee e = new Employee();
        e.setFirstName(dto.firstName());
        e.setMiddleName(dto.middleName());
        e.setLastName(dto.lastName());
        e.setPhone(dto.phone());
        e.setPinCode(dto.pinCode());

        employeeRepository.save(e);
    }

    public String getFirstLastName(int pin) {
        Optional<Employee> employee = employeeRepository.findByPinCode(pin);
        if (employee.isPresent()) {
            return employee.get().getFirstName() + " " + employee.get().getLastName();
        } else {
            throw new EmployeeException(HttpStatus.NOT_FOUND, "Работник с пинкодом " + pin + " не найден");
        }
    }

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeDtoMapper)
                .toList();
    }


}
