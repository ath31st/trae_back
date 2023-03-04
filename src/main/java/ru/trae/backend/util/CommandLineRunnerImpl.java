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
                    "89183331212", List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)),
            new NewEmployeeDto("Николай", "Игоревич", "Иванов",
                    "89283332121", List.of(3L, 4L, 5L, 6L, 7L)),
            new NewEmployeeDto("Владимир", "Васильевич", "Петров",
                    "89174445632", List.of(8L, 9L)),
            new NewEmployeeDto("Александр", "Григорьевич",
                    "Красильников", "89271238899", List.of(8L, 9L)),
            new NewEmployeeDto("Никита", "Владимирович", "Бондаренко",
                    "89153334567", List.of(1L)),
            new NewEmployeeDto("Валентин", "Александрович", "Плотников",
                    "89347778294", List.of(1L, 5L, 6L, 7L)),
            new NewEmployeeDto("Петр", "Иванович", "Абраменко",
                    "89183454829", List.of(1L, 2L, 3L, 4L)),
            new NewEmployeeDto("Григорий", "Олегович", "Костромин",
                    "89123345993", List.of(4L, 5L, 6L, 7L)),
            new NewEmployeeDto("Егор", "Антонович", "Карпов",
                    "89155675993", List.of(1L, 2L, 5L, 6L, 7L)),
            new NewEmployeeDto("Антон", "Петрович", "Рыбин",
                    "89132245911", List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)),
            new NewEmployeeDto("Аркадий", "Олегович", "Павлов",
                    "89113335798", List.of(1L, 2L, 3L, 4L, 5L)),
            new NewEmployeeDto("Степан", "Степанович", "Никитин",
                    "89223245913", List.of(1L, 2L, 5L, 6L, 7L, 8L, 9L))
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
            "Мишин", "89991112233",
            "man");

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
              40,
              1L,
              "Шишкина М.М.",
              List.of(
                      new NewOperationDto("Раскрой", 1, 10),
                      new NewOperationDto("Кромка", 2, 20),
                      new NewOperationDto("Фрезеровка", 4, 30),
                      new NewOperationDto("Присадка", 3, 40),
                      new NewOperationDto("Сборка", 6, 50),
                      new NewOperationDto("Покраска", 7, 60),
                      new NewOperationDto("Отгрузка", 9, 70)));

      NewProjectDto dto2 = new NewProjectDto(
              346,
              "Дверь",
              25,
              1L,
              "ГосСтройБыт",
              List.of(
                      new NewOperationDto("Раскрой", 1, 10),
                      new NewOperationDto("Кромка", 2, 20),
                      new NewOperationDto("Фрезеровка", 4, 30),
                      new NewOperationDto("Сборка", 6, 40),
                      new NewOperationDto("Особый вид покраски - лакировка",
                              7, 50),
                      new NewOperationDto("Отгрузка", 9, 60)));

      NewProjectDto dto3 = new NewProjectDto(
              284,
              "Стол",
              35,
              1L,
              "Петров В.Г.",
              List.of(
                      new NewOperationDto("Раскрой", 1, 10),
                      new NewOperationDto("Кромка", 2, 20),
                      new NewOperationDto("Фрезеровка", 4, 30),
                      new NewOperationDto("Сборка", 6, 40),
                      new NewOperationDto("Покраска", 7, 50),
                      new NewOperationDto("Покраска", 7, 60),
                      new NewOperationDto("Отгрузка", 9, 70)));

      projectService.saveNewProject(dto1);
      projectService.saveNewProject(dto2);
      projectService.saveNewProject(dto3);
    }
  }
}
