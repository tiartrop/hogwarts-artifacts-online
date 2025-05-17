package edu.tcu.cs.hogwarts_artifacts_online.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import edu.tcu.cs.hogwarts_artifacts_online.client.rediscache.RedisCacheClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

  private final RedisCacheClient redisCacheClient;

  public JwtInterceptor(RedisCacheClient redisCacheClient) {
    this.redisCacheClient = redisCacheClient;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // Get the token from the request header.
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Jwt jwt = (Jwt) authentication.getPrincipal();

      // Retrieve the user ID from the JWT and check if the token is in the whitelist.
      String userId = jwt.getClaimAsString("userId").toString();
      if(!this.redisCacheClient.isTokenInWhiteList(userId, jwt.getTokenValue())) {
        throw new BadCredentialsException("Invalid token");
      }
    }
    return true;
  }

}
