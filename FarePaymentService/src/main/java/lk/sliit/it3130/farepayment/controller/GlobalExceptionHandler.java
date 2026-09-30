package lk.sliit.it3130.farepayment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 400,
                "error", "Validation failed",
                "details", errors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> illegalArgument(IllegalArgumentException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 404,
                "error", ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> illegalState(IllegalStateException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 409,
                "error", ex.getMessage()
        );
    }
}
