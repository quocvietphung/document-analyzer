package orgaplan.beratung.kreditunterlagen.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import orgaplan.beratung.kreditunterlagen.response.ExceptionResponse;

import java.time.LocalDateTime;
import orgaplan.beratung.kreditunterlagen.exception.UserExceptions.UserNotFoundException;
import orgaplan.beratung.kreditunterlagen.exception.UserExceptions.InvalidUserDataException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ExceptionResponse response = new ExceptionResponse(LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(), "User Not Found", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidUserDataException(InvalidUserDataException ex) {
        ExceptionResponse response = new ExceptionResponse(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), "Invalid User Data", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
