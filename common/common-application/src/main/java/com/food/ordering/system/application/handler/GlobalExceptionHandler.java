package com.food.ordering.system.application.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ResponseBody
  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorDto handleException(Exception exception) {
    log.error(exception.getMessage(), exception);
    return ErrorDto.builder()
        .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("Unexpected error!")
        .build();
  }

  @ResponseBody
  @ExceptionHandler(value = {ValidationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto handleException(ValidationException validationException) {
    ErrorDto errorDto;
    if (validationException instanceof ConstraintViolationException) {
      String violations =
          extractViolationsFromException((ConstraintViolationException) validationException);
      log.error(violations, validationException);
      errorDto = ErrorDto.builder()
          .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
          .message(violations)
          .build();
    } else {
      log.error(validationException.getMessage(), validationException);
      errorDto = ErrorDto.builder()
          .message(validationException.getMessage())
          .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
          .build();
    }
    return errorDto;
  }

  private String extractViolationsFromException(ConstraintViolationException validationException) {
    return validationException.getConstraintViolations()
        .stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.joining("--"));
  }
}
