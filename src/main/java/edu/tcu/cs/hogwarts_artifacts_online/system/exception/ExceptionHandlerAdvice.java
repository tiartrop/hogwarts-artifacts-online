package edu.tcu.cs.hogwarts_artifacts_online.system.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

  @ExceptionHandler(ObjectNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result handlerObjectNotFoundException(ObjectNotFoundException ex) {
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

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleIllegalArgumentException(IllegalArgumentException ex) {
    return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are invalid, see data for details", ex.getMessage());
  }

  @ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class })
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result handlerAuthenticationException(Exception ex) {
    return new Result(false, StatusCode.UNAUTHORIZED, "username or password is incorrect.", ex.getMessage());
  }

  @ExceptionHandler(InsufficientAuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
      return new Result(false, StatusCode.UNAUTHORIZED, "Login credentials are missing.", ex.getMessage());
  }

  @ExceptionHandler(AccountStatusException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result handlerAccountStatusException(AccountStatusException ex) {
    return new Result(false, StatusCode.UNAUTHORIZED, "User account is abnormal.", ex.getMessage());
  }

  @ExceptionHandler(InvalidBearerTokenException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result handlerInvalidBearerTokenException(InvalidBearerTokenException ex) {
    return new Result(false, StatusCode.UNAUTHORIZED, "The access token provided is expired, revoked, malformed, or invalid for other reasons.", ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Result handlerAccessDeniedException(AccessDeniedException ex) {
    return new Result(false, StatusCode.FORBIDDEN, "No permission.", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  Result handlerOtherException(Exception ex) {
    return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error", ex.getMessage());
  }

}
