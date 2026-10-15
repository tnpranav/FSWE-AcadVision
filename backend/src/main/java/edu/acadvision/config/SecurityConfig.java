package edu.acadvision.config;

import edu.acadvision.security.jwt.JwtAuthenticationEntryPoint;
import edu.acadvision.security.jwt.JwtAuthenticationFilter;
import edu.acadvision.security.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  @Value("${app.frontend-origin:http://localhost:5173}")
  private String frontendOrigin;

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
  private final CustomUserDetailsService userDetailsService;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthFilter,
      JwtAuthenticationEntryPoint jwtAuthEntryPoint,
      CustomUserDetailsService userDetailsService
  ) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        // Public endpoints: Auth, Health, CORS Preflight
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/health", "/actuator/health").permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        // Server-Side RBAC Enforcement:
        // Admin workspace endpoints
        .requestMatchers("/api/admin/**").hasRole("ADMIN")

        // Faculty workspace endpoints (Admin also allowed for oversight/preview)
        .requestMatchers("/api/faculty/**").hasAnyRole("FACULTY", "ADMIN")

        // Student workspace endpoints (Student, Faculty, Admin)
        .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")

        // Shared authenticated modules
        .requestMatchers("/api/announcements/**", "/api/notifications/**", "/api/support/**").authenticated()

        // All other requests require authentication
        .anyRequest().authenticated()
      )
      .authenticationProvider(authenticationProvider())
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(frontendOrigin, "http://localhost:5173", "http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With", "Accept"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
