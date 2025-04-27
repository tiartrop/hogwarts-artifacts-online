package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * This class handles unsuccessful basic authentication.
 */
@Component
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver resolver;

  public CustomBasicAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    response.addHeader("WWW-Authenticate", "Basic realm=\"Realm\"");

    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if (!StringUtils.hasText(username)) {
      this.resolver.resolveException(request, response, null, new IllegalArgumentException("username is required."));
    } else if (!StringUtils.hasText(password)) {
      this.resolver.resolveException(request, response, null, new IllegalArgumentException("password is required."));
    } else {
      this.resolver.resolveException(request, response, null, authException);
    }
  }

}
