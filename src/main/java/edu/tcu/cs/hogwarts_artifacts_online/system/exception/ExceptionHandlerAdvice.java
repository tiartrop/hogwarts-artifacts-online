package edu.tcu.cs.hogwarts_artifacts_online.system.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import edu.tcu.cs.hogwarts_artifacts_online.wizard.WizardNotFoundException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

  @ExceptionHandler(ArtifactNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result handlerArtifactNotFoundException(ArtifactNotFoundException ex) {
    return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(WizardNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result handlerWizardNotFoundException(WizardNotFoundException ex) {
    return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Result handleValidException(MethodArgumentNotValidException ex) {
    List<ObjectError> errors = ex.getBindingResult().getAllErrors();
    Map<String, String> map = new HashMap<>(errors.size());
    errors.forEach(error -> {
      String key = ((FieldError) error).getField();
      String val = error.getDefaultMessage();
      map.put(key, val);
    });
    return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are invalid, see data for details", map);
  }

}
