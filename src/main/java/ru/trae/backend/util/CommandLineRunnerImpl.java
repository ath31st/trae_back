/*
 * Copyright (c) 2023. Vladimir Olennikov.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.trae.backend.util;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.trae.backend.dto.employee.NewEmployeeDto;
import ru.trae.backend.dto.manager.ManagerRegisterDto;
import ru.trae.backend.dto.operation.NewOperationDto;
import ru.trae.backend.dto.project.NewProjectDto;
import ru.trae.backend.dto.type.NewTypeWorkDto;
import ru.trae.backend.service.EmployeeService;
import ru.trae.backend.service.ManagerService;
import ru.trae.backend.service.ProjectService;
import ru.trae.backend.service.TypeWorkService;

/**
 * Utility class for filling the database with temporary data.
 *
 * @author Vladimir Olennikov
 */
@Component
@RequiredArgsConstructor
public class CommandLineRunnerImpl implements CommandLineRunner {
  private final EmployeeService employeeService;
  private final ManagerService managerService;
  private final ProjectService projectService;
  private final TypeWorkService typeWorkService;

  @Override
  public void run(String... args) {
    insertTypeWork();
    insertEmployees();
    insertManager();
    insertProject();
  }

  /**
   * Inserting data types of work.
   */
  public void insertTypeWork() {
    List<NewTypeWorkDto> list = List.of(
        new NewTypeWorkDto("Раскрой"),
        new NewTypeWorkDto("Кромка"),
        new NewTypeWorkDto("Присадка"),
        new NewTypeWorkDto("Фрезеровка"),
        new NewTypeWorkDto("Склейка"),
        new NewTypeWorkDto("Сборка"),
        new NewTypeWorkDto("Шлифовка/покраска"),
        new NewTypeWorkDto("Упаковка"),
        new NewTypeWorkDto("Отгрузка"));

    list.stream()
        .filter(t -> !typeWorkService.existsTypeByName(t.name()))
        .forEach(typeWorkService::saveNewTypeWork);
  }

  /**
   * Inserting employee data.
   */
  public void insertEmployees() {
    List<NewEmployeeDto> list = List.of(
        new NewEmployeeDto("Иван", "Петрович", "Шилов",
            "+7 (918) 333 1212", LocalDateTime.parse("2021-03-22T11:00:33"),
            List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)),
        new NewEmployeeDto("Николай", "Игоревич", "Иванов",
            "+7 (928) 333 2121", LocalDateTime.parse("2004-07-17T12:00:33"),
            List.of(3L, 4L, 5L, 6L, 7L)),
        new NewEmployeeDto("Владимир", "Васильевич", "Петров",
            "+7 (917) 444 5632", LocalDateTime.parse("2009-12-10T13:00:33"),
            List.of(8L, 9L)),
        new NewEmployeeDto("Александр", "Григорьевич",
            "Красильников", "+7 (927) 123 8899",
            LocalDateTime.parse("2021-04-10T13:00:33"), List.of(8L, 9L)),
        new NewEmployeeDto("Никита", "Владимирович", "Бондаренко",
            "+7 (915) 333 4567", LocalDateTime.parse("2002-02-13T14:00:33"),
            List.of(1L)),
        new NewEmployeeDto("Валентин", "Александрович", "Плотников",
            "+7 (934) 777 8294", LocalDateTime.parse("2007-07-10T15:00:33"),
            List.of(1L, 5L, 6L, 7L)),
        new NewEmployeeDto("Петр", "Иванович", "Абраменко",
            "+7 (918) 345 4829", LocalDateTime.parse("2020-01-10T16:00:33"),
            List.of(1L, 2L, 3L, 4L)),
        new NewEmployeeDto("Григорий", "Олегович", "Костромин",
            "+7 (912) 334 5993", LocalDateTime.parse("2010-11-11T19:00:33"),
            List.of(4L, 5L, 6L, 7L)),
        new NewEmployeeDto("Егор", "Антонович", "Карпов",
            "+7 (915) 567 5993", LocalDateTime.parse("2021-06-18T10:00:33"),
            List.of(1L, 2L, 5L, 6L, 7L)),
        new NewEmployeeDto("Антон", "Петрович", "Рыбин",
            "+7 (913) 224 5911", LocalDateTime.parse("2005-08-15T14:00:33"),
            List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)),
        new NewEmployeeDto("Аркадий", "Олегович", "Павлов",
            "+7 (911) 333 5798", LocalDateTime.parse("2008-02-11T12:00:33"),
            List.of(1L, 2L, 3L, 4L, 5L)),
        new NewEmployeeDto("Степан", "Степанович", "Никитин",
            "+7 (922) 324 5913", LocalDateTime.parse("2022-08-10T16:00:33"),
            List.of(1L, 2L, 5L, 6L, 7L, 8L, 9L))
    );

    list.stream()
        .filter(e -> !employeeService.existsByCredentials(e.firstName(),
            e.middleName(),
            e.lastName()))
        .forEach(employeeService::saveNewEmployee);
  }

  /**
   * Inserting manager data.
   */
  public void insertManager() {
    ManagerRegisterDto dto = new ManagerRegisterDto("Михаил", "Михаилович",
        "Мишин", "+7 (999) 111 2233",
        "Manager8", LocalDateTime.parse("2022-01-10T11:00:33"));

    if (!managerService.existsManagerByUsername(dto.username())) {
      System.out.println("=================================");
      System.out.println(managerService.saveNewManager(dto));
      System.out.println("=================================");
    }
  }

  /**
   * Inserting project data.
   */
  public void insertProject() {
    if (projectService.getAllProjects().size() == 0) {
      NewProjectDto dto1 = new NewProjectDto(
          345,
          "Шкаф",
          LocalDateTime.parse("2023-05-10T11:22:33"),
          "Шишкина М.М.",
          "Комментарий",
          List.of(
              new NewOperationDto("Раскрой", 1),
              new NewOperationDto("Кромка", 2),
              new NewOperationDto("Фрезеровка", 4),
              new NewOperationDto("Присадка", 3),
              new NewOperationDto("Сборка", 6),
              new NewOperationDto("Покраска", 7)));

      NewProjectDto dto2 = new NewProjectDto(
          346,
          "Дверь",
          LocalDateTime.parse("2023-04-15T18:21:33"),
          "ГосСтройБыт",
          "Комментарий",
          List.of(
              new NewOperationDto("Раскрой", 1),
              new NewOperationDto("Кромка", 2),
              new NewOperationDto("Фрезеровка", 4),
              new NewOperationDto("Сборка", 6),
              new NewOperationDto("Лакировка", 7)));

      NewProjectDto dto3 = new NewProjectDto(
          284,
          "Стол",
          LocalDateTime.parse("2023-04-18T08:23:33"),
          "Петров В.Г.",
          null,
          List.of(
              new NewOperationDto("Раскрой", 1),
              new NewOperationDto("Кромка", 2),
              new NewOperationDto("Фрезеровка", 4),
              new NewOperationDto("Сборка", 6),
              new NewOperationDto("Покраска", 7),
              new NewOperationDto("Покраска", 7)));

      projectService.saveNewProject(dto1, "Manager8");
      projectService.saveNewProject(dto2, "Manager8");
      projectService.saveNewProject(dto3, "Manager8");
    }
  }
}
