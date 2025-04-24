package edu.tcu.cs.hogwarts_artifacts_online.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfiguration {

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
              .requestMatchers(HttpMethod.GET, this.baseUrl + "/artifacts/**").permitAll()
              .requestMatchers(HttpMethod.GET, this.baseUrl + "/users/**").hasAnyAuthority("ROLE_admin")
              .requestMatchers(HttpMethod.POST, this.baseUrl + "/users/").hasAnyAuthority("ROLE_admin")
              .requestMatchers(HttpMethod.PUT, this.baseUrl + "/users/**").hasAnyAuthority("ROLE_admin")
              .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/users/**").hasAnyAuthority("ROLE_admin")
              .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
              // Disallow everything else.
              .anyRequest().authenticated() // Always a good idea to put this as last.
            )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())) // This is for H2 browser console access.
            .csrf(csrf -> csrf.disable())
            .httpBasic(Customizer.withDefaults())
            .build();
  }

  @Bean
  public PasswordEncoder PasswordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

}
