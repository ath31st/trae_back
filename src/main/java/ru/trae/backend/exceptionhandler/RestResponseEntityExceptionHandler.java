/*
 * Copyright (c) 2023. Vladimir Olennikov.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.trae.backend.exceptionhandler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.trae.backend.exceptionhandler.exception.AbstractException;
import ru.trae.backend.exceptionhandler.exception.CustomJwtVerificationException;
import ru.trae.backend.exceptionhandler.exception.EmployeeException;
import ru.trae.backend.exceptionhandler.exception.LoginCredentialException;
import ru.trae.backend.exceptionhandler.exception.ManagerException;
import ru.trae.backend.exceptionhandler.exception.OperationException;
import ru.trae.backend.exceptionhandler.exception.PayloadPieceException;
import ru.trae.backend.exceptionhandler.exception.ProjectException;
import ru.trae.backend.exceptionhandler.exception.TypeWorkException;
import ru.trae.backend.exceptionhandler.exception.WorkingShiftException;

/**
 * This class is a ControllerAdvice used to provide centralized exception handling across
 * all Controllers.
 * It extends ResponseEntityExceptionHandler to provide a more robust implementation of
 * exception handling.
 *
 * @author Vladimir Olennikov
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(EmployeeException.class)
  protected ResponseEntity<Response> handleException(EmployeeException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(ManagerException.class)
  protected ResponseEntity<Response> handleException(ManagerException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(WorkingShiftException.class)
  protected ResponseEntity<Response> handleException(WorkingShiftException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(ProjectException.class)
  protected ResponseEntity<Response> handleException(ProjectException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(OperationException.class)
  protected ResponseEntity<Response> handleException(OperationException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(TypeWorkException.class)
  protected ResponseEntity<Response> handleException(TypeWorkException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(PayloadPieceException.class)
  protected ResponseEntity<Response> handleException(PayloadPieceException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(LoginCredentialException.class)
  protected ResponseEntity<Response> handleException(LoginCredentialException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(CustomJwtVerificationException.class)
  protected ResponseEntity<Response> handleException(CustomJwtVerificationException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Response> handleValidException(ConstraintViolationException e) {
    Response response = Response.builder()
            .timestamp(LocalDateTime.now().toString())
            .error(e.getMessage())
            .status(HttpStatus.BAD_REQUEST)
            .build();
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(JWTVerificationException.class)
  protected ResponseEntity<Response> handleJwtException(JWTVerificationException e) {
    Response response = Response.builder()
            .timestamp(LocalDateTime.now().toString())
            .error(e.getMessage())
            .status(HttpStatus.BAD_REQUEST)
            .build();
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
          MethodArgumentNotValidException ex,
          HttpHeaders headers,
          HttpStatus status,
          WebRequest request) {

    String errorString = ex.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));

    Response response = Response.builder()
            .timestamp(LocalDateTime.now().toString())
            .error(errorString)
            .status(status)
            .build();

    return new ResponseEntity<>(response, status);
  }

  private Response buildResponse(AbstractException e) {
    return Response.builder()
            .timestamp(LocalDateTime.now().toString())
            .error(e.getMessage())
            .status(e.getStatus())
            .build();
  }
}
