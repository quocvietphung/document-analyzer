package orgaplan.beratung.kreditunterlagen.config;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import orgaplan.beratung.kreditunterlagen.response.ExceptionResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionConfig {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionConfig.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(Exception ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        String errorMessage = rootCause.getMessage();

        ExceptionResponse response = new ExceptionResponse(LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Interner Serverfehler", errorMessage);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        logger.error("Validierungsfehler: {}", String.join(", ", errors), ex);

        ExceptionResponse response = new ExceptionResponse(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), "Validierungsfehler", String.join(", ", errors));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        logger.error("Verstoß gegen die Einschränkungen: {}", String.join(", ", errors), ex);

        ExceptionResponse response = new ExceptionResponse(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), "Validierungsfehler", String.join(", ", errors));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMessage = "Verstoß gegen die Datenintegrität: " + ex.getMostSpecificCause().getMessage();

        logger.error("Verstoß gegen die Datenintegrität: {}", errorMessage, ex);

        ExceptionResponse response = new ExceptionResponse(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), "Verstoß gegen die Datenintegrität", errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}