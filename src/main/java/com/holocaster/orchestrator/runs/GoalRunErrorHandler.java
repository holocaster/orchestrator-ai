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
                .body(Map.of("message", "Já existe uma execução em andamento."));
    }

    @ExceptionHandler(GoalRunNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(GoalRunNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalidGoal(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", "O objetivo deve conter entre 1 e 10.000 caracteres."));
    }
}
