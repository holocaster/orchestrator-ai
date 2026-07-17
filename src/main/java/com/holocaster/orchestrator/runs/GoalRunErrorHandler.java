package com.holocaster.orchestrator.runs;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GoalRunErrorHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Another Goal Run is already running."));
    }

    @ExceptionHandler(GoalRunNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(GoalRunNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalidGoal(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", "Goal must contain between 1 and 10000 characters."));
    }
}
