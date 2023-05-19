package ru.trae.backend.dto.report;

import java.time.LocalDate;
import java.util.List;
import ru.trae.backend.dto.employee.EmployeeIdFirstLastNameDto;
import ru.trae.backend.projection.WorkingShiftEmployeePercentage;

/**
 * Data transfer object for reporting working shifts for a specific period.
 *
 * @author Vladimir Olennikov
 */
public record ReportWorkingShiftForPeriodDto(
    LocalDate startPeriod,
    LocalDate endPeriod,
    List<EmployeeIdFirstLastNameDto> shortEmployeeDtoList,
    List<WorkingShiftEmployeePercentage> workingShiftEmployeePercentageList
) {
}
