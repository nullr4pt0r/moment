package io.nullr4pt0r.moment.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponses> handleRuntimeException(RuntimeException e) {
        log.error("Exception: {} | Cause: {}", e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponses(e.getMessage(), "FAILURE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponses> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Bad Request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponses(e.getMessage(), "FAILURE"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponses> handleGenericException(Exception e) {
        log.error("Unexpected Error: {} | Cause: {}", e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponses("An unexpected error occurred", "FAILURE"));
    }

}

@Data
@AllArgsConstructor
class ErrorResponses{
    String message;
    String status;
}
