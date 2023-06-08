/*
 * Copyright (c) 2023. Vladimir Olennikov.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.trae.backend.service;

import static ru.trae.backend.util.Constant.NOT_FOUND_CONST;
import static ru.trae.backend.util.Constant.WRONG_PARAMETER;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.trae.backend.dto.employee.EmployeeIdFirstLastNameDto;
import ru.trae.backend.dto.employee.EmployeeIdTotalPartsDto;
import ru.trae.backend.dto.mapper.ProjectForReportDtoMapper;
import ru.trae.backend.dto.project.ProjectForReportDto;
import ru.trae.backend.dto.report.DeadlineReq;
import ru.trae.backend.dto.report.ReportDeadlineDto;
import ru.trae.backend.dto.report.ReportProjectsForPeriodDto;
import ru.trae.backend.dto.report.ReportWorkingShiftForPeriodDto;
import ru.trae.backend.dto.report.SecondResponseSubDto;
import ru.trae.backend.dto.report.ThirdResponseSubDto;
import ru.trae.backend.entity.task.Operation;
import ru.trae.backend.entity.task.Project;
import ru.trae.backend.entity.user.Employee;
import ru.trae.backend.exceptionhandler.exception.ReportException;
import ru.trae.backend.projection.WorkingShiftEmployeeDto;
import ru.trae.backend.util.ReportParameter;

/**
 * Service class for generating reports.
 *
 * @author Vladimir Olennikov
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
  private final WorkingShiftService workingShiftService;
  private final EmployeeService employeeService;
  private final ProjectService projectService;
  private final OperationService operationService;
  private final ProjectForReportDtoMapper projectForReportDtoMapper;
  
  /**
   * Generates a report of working shifts for a specific period.
   *
   * @param startOfPeriod The start date of the period.
   * @param endOfPeriod   The end date of the period.
   * @param employeeIds   The set of concrete employee ids
   * @return The {@link ReportWorkingShiftForPeriodDto} containing the report data.
   */
  public ReportWorkingShiftForPeriodDto reportWorkingShiftForPeriod(
      LocalDate startOfPeriod, LocalDate endOfPeriod, Set<Long> employeeIds) {
    
    checkStartEndDates(startOfPeriod, endOfPeriod);
    
    List<WorkingShiftEmployeeDto> workingShiftList =
        workingShiftService.getWorkingShiftEmployeeByEmpIds(
            startOfPeriod, endOfPeriod, employeeIds);
    
    List<EmployeeIdFirstLastNameDto> shortEmployeeDtoList = employeeService.getEmployeeDtoByListId(
        workingShiftList.stream()
            .map(WorkingShiftEmployeeDto::getEmployeeId)
            .distinct()
            .toList());
    
    List<EmployeeIdTotalPartsDto> employeeIdTotalPartsDtoList = workingShiftList.stream()
        .collect(Collectors.groupingBy(WorkingShiftEmployeeDto::getEmployeeId,
            Collectors.summingDouble(WorkingShiftEmployeeDto::getPartOfShift)))
        .entrySet()
        .stream()
        .map(e -> new EmployeeIdTotalPartsDto(e.getKey(), e.getValue().floatValue()))
        .toList();
    
    return new ReportWorkingShiftForPeriodDto(
        startOfPeriod,
        endOfPeriod,
        shortEmployeeDtoList,
        workingShiftList,
        employeeIdTotalPartsDtoList);
  }
  
  /**
   * Generates a report of projects for a given period.
   *
   * @param startOfPeriod The start date of the period.
   * @param endOfPeriod   The end date of the period.
   * @return A DTO (Data Transfer Object) representing the report for the specified period.
   */
  public ReportProjectsForPeriodDto reportProjectsForPeriod(
      LocalDate startOfPeriod, LocalDate endOfPeriod) {
    
    checkStartEndDates(startOfPeriod, endOfPeriod);
    
    List<Project> projects = projectService.findProjectsForPeriod(startOfPeriod, endOfPeriod);
    List<ProjectForReportDto> projectForReportDtoList = projects.stream()
        .map(projectForReportDtoMapper)
        .toList();
    
    return new ReportProjectsForPeriodDto(
        startOfPeriod, endOfPeriod, LocalDate.now(), projectForReportDtoList);
  }
  
  public ReportDeadlineDto reportDeadlines(DeadlineReq req) {
    
    checkCorrectParametersRequest(req);
    
    ReportDeadlineDto report = new ReportDeadlineDto();
    report.setFirstRespId(req.valueOfFirstParameter());
    
    List<Operation> ops;
    if (req.firstParameter().ordinal() == 1) {
      ops = operationService.getOperationsByIds(Set.of(req.valueOfFirstParameter()));
    } else if (req.secondParameter().ordinal() == 1) {
      ops = operationService.getOperationsByIds(req.valuesOfSecondParameter());
    } else {
      ops = operationService.getOperationsByIds(req.valuesOfThirdParameter());
    }
    
    checkNotEmptyListOps(ops);
    
    switch (req.firstParameter()) {
      
      case PROJECT -> {
        checkCorrectProjectIdFromReqAndOp(req.valueOfFirstParameter(), ops.get(0));
        
        report.setFirstRespValue(String.valueOf(ops.get(0).getProject().getNumber()));
        switch (req.secondParameter()) {
          case OPERATION -> addToPrReportSecondSubDtoByOperations(req.valueOfFirstParameter(),
              req.valuesOfSecondParameter(), req.valuesOfThirdParameter(), report, ops);
          case EMPLOYEE -> addToPrReportSecondSubDtoByEmployees(req.valueOfFirstParameter(),
              req.valuesOfSecondParameter(), req.valuesOfThirdParameter(), report, ops);
          default -> throw new ReportException(HttpStatus.BAD_REQUEST, WRONG_PARAMETER.value);
        }
      }
      
      case OPERATION -> {
        report.setFirstRespValue(ops.get(0).getName());
        switch (req.secondParameter()) {
          case PROJECT -> addToOpReportSecondSubDtoByProject(
              req.valuesOfSecondParameter(), req.valuesOfThirdParameter(), report, ops.get(0));
          case EMPLOYEE -> addToOpReportSecondSubDtoByEmployee(
              req.valuesOfSecondParameter(), req.valuesOfThirdParameter(), report, ops.get(0));
          default -> throw new ReportException(HttpStatus.BAD_REQUEST, WRONG_PARAMETER.value);
        }
      }
      
      case EMPLOYEE -> {
        checkNotNullEmpInOp(ops.get(0));
        checkCorrectEmpIdFromReqAndEmpIdFromOp(req.valueOfFirstParameter(), ops.get(0));
        
        report.setFirstRespValue(ops.get(0).getEmployee().getLastName());
        switch (req.secondParameter()) {
          case PROJECT ->
              addToEmpReportSecondSubDtoByProjects(req.valuesOfSecondParameter(), report, ops);
          case OPERATION ->
              addToEmpReportSecondSubDtoByOperations(req.valuesOfSecondParameter(), report, ops);
          default -> throw new ReportException(HttpStatus.BAD_REQUEST, WRONG_PARAMETER.value);
        }
      }
      
      default -> throw new ReportException(HttpStatus.BAD_REQUEST, "Wrong values in parameters");
    }
    
    return report;
  }
  
  private void checkCorrectProjectIdFromReqAndOp(long projectIdFromReq, Operation o) {
    if (projectIdFromReq != o.getProject().getId()) {
      throw new ReportException(HttpStatus.BAD_REQUEST,
          "The project id does not match the project id from the operation");
    }
  }
  
  private void checkCorrectEmpIdFromReqAndEmpIdFromOp(long employeeId, Operation o) {
    if (o.getEmployee().getId() != employeeId) {
      throw new ReportException(HttpStatus.BAD_REQUEST, "The operation with id: " + o.getId()
          + " from the selection does not match the specified employee with id: " + employeeId);
    }
  }
  
  private void checkNotNullEmpInOp(Operation o) {
    if (o.getEmployee() == null) {
      throw new ReportException(HttpStatus.BAD_REQUEST,
          "The operation from the selection does not have an employee");
    }
  }
  
  private void checkNotEmptyListOps(List<Operation> ops) {
    if (ops.isEmpty()) {
      throw new ReportException(HttpStatus.BAD_REQUEST,
          "the parameter values are not correct, the final result is empty");
    }
  }
  
  private void checkCorrectParametersRequest(DeadlineReq req) {
    Set<ReportParameter> reqSet = new HashSet<>();
    reqSet.add(req.firstParameter());
    reqSet.add(req.secondParameter());
    reqSet.add(req.thirdParameter());
    
    if (reqSet.size() != 3) {
      throw new ReportException(HttpStatus.CONFLICT, "Parameter values are repeated");
    }
  }
  
  private void addToPrReportSecondSubDtoByEmployees(
      Long firstValue,
      Set<Long> secondValues,
      Set<Long> thirdValues,
      ReportDeadlineDto report,
      List<Operation> ops) {
    report.setSecondRespValues(secondValues.stream()
        .map(eId -> {
          Employee e = ops.stream()
              .filter(o -> o.getEmployee() != null)
              .filter(o -> Objects.equals(o.getEmployee().getId(), eId)
                  && Objects.equals(o.getProject().getId(), firstValue))
              .findFirst()
              .orElseThrow(() -> new ReportException(HttpStatus.BAD_REQUEST,
                  "Employee with id: " + eId + NOT_FOUND_CONST.value + " in project with id: "
                      + firstValue))
              .getEmployee();
          return new SecondResponseSubDto(e.getId(), e.getLastName(), ops.stream()
              .filter(o -> Objects.equals(o.getEmployee().getId(), e.getId())
                  && thirdValues.contains(o.getId()))
              .map(o -> new ThirdResponseSubDto(o.getId(), o.getName(),
                  o.getPlannedEndDate(), o.getRealEndDate()))
              .toList());
        }).toList());
  }
  
  private void addToPrReportSecondSubDtoByOperations(
      Long firstValue,
      Set<Long> secondValues,
      Set<Long> thirdValues,
      ReportDeadlineDto report,
      List<Operation> ops) {
    report.setSecondRespValues(secondValues.stream()
        .map(oId -> {
              Operation op = ops.stream()
                  .filter(o -> Objects.equals(o.getId(), oId)
                      && Objects.equals(o.getProject().getId(), firstValue))
                  .findFirst()
                  .orElseThrow(() -> new ReportException(HttpStatus.BAD_REQUEST,
                      "Operation with id: " + oId + NOT_FOUND_CONST.value + " in project with id: "
                          + firstValue));
              
              checkNotNullEmpInOp(op);
              thirdValues.forEach(e -> checkCorrectEmpIdFromReqAndEmpIdFromOp(e, op));
              
              return new SecondResponseSubDto(op.getId(), op.getName(),
                  List.of(new ThirdResponseSubDto(
                      op.getEmployee().getId(),
                      op.getEmployee().getLastName(),
                      op.getPlannedEndDate(),
                      op.getRealEndDate())));
            }
        ).toList());
  }
  
  private void addToEmpReportSecondSubDtoByOperations(
      Set<Long> secondValues, ReportDeadlineDto report, List<Operation> ops) {
    report.setSecondRespValues(secondValues.stream()
        .map(oId -> {
          Operation op = ops.stream()
              .filter(o -> Objects.equals(o.getId(), oId))
              .findFirst()
              .orElseThrow(() -> new ReportException(HttpStatus.BAD_REQUEST,
                  "Operation with id: " + oId + NOT_FOUND_CONST.value));
          Project pr = ops.stream()
              .filter(o -> Objects.equals(o.getProject().getId(), op.getProject().getId()))
              .findFirst()
              .orElseThrow(() -> new ReportException(HttpStatus.BAD_REQUEST,
                  "Project with id: " + op.getProject().getId() + NOT_FOUND_CONST))
              .getProject();
          return new SecondResponseSubDto(
              op.getId(), op.getName(),
              List.of(new ThirdResponseSubDto(pr.getId(), String.valueOf(pr.getNumber()),
                  op.getPlannedEndDate(), op.getRealEndDate())));
        })
        .toList());
  }
  
  private void addToEmpReportSecondSubDtoByProjects(
      Set<Long> secondValues, ReportDeadlineDto report, List<Operation> ops) {
    report.setSecondRespValues(secondValues.stream()
        .map(pId -> {
          Project pr = ops.stream()
              .filter(o -> Objects.equals(o.getProject().getId(), pId))
              .findFirst()
              .orElseThrow(() -> new ReportException(HttpStatus.BAD_REQUEST,
                  "Project with id: " + pId + NOT_FOUND_CONST.value))
              .getProject();
          return new SecondResponseSubDto(
              pr.getId(), String.valueOf(pr.getNumber()), ops.stream()
              .filter(o -> Objects.equals(o.getProject().getId(), pId))
              .map(o -> new ThirdResponseSubDto(
                  o.getId(), o.getName(), o.getPlannedEndDate(), o.getRealEndDate()))
              .toList());
        })
        .toList());
  }
  
  private void addToOpReportSecondSubDtoByProject(
      Set<Long> secondValues,
      Set<Long> thirdValues,
      ReportDeadlineDto report,
      Operation op) {
    
    checkNotNullEmpInOp(op);
    secondValues.forEach(p -> checkCorrectProjectIdFromReqAndOp(p, op));
    thirdValues.forEach(e -> checkCorrectEmpIdFromReqAndEmpIdFromOp(e, op));
    
    report.setSecondRespValues(
        List.of(new SecondResponseSubDto(
            op.getProject().getId(),
            String.valueOf(op.getProject().getNumber()),
            List.of(new ThirdResponseSubDto(
                op.getEmployee().getId(),
                op.getEmployee().getLastName(),
                op.getPlannedEndDate(),
                op.getRealEndDate())))));
  }
  
  private void addToOpReportSecondSubDtoByEmployee(
      Set<Long> secondValues,
      Set<Long> thirdValues,
      ReportDeadlineDto report,
      Operation op) {
    
    checkNotNullEmpInOp(op);
    secondValues.forEach(p -> checkCorrectProjectIdFromReqAndOp(p, op));
    thirdValues.forEach(e -> checkCorrectEmpIdFromReqAndEmpIdFromOp(e, op));
    
    report.setSecondRespValues(
        List.of(new SecondResponseSubDto(
            op.getEmployee().getId(),
            op.getEmployee().getLastName(),
            List.of(new ThirdResponseSubDto(
                op.getProject().getId(),
                String.valueOf(op.getProject().getNumber()),
                op.getPlannedEndDate(),
                op.getRealEndDate())))));
  }
  
  private void checkStartEndDates(LocalDate startOfPeriod, LocalDate endOfPeriod) {
    if (startOfPeriod.isAfter(endOfPeriod)) {
      throw new ReportException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date.");
    }
  }
}
