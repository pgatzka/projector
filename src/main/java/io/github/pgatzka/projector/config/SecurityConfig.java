package io.github.pgatzka.projector.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(
                    "/", "/index.html", "/favicon.ico",
                    "/assets/**", "/static/**",
                    "/error",
                    "/login", "/setup",
                    "/api/health",
                    "/api/setup",
                    "/api/setup-required",
                    "/api/login",
                    "/actuator/health", "/actuator/health/**",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/v3/api-docs/**",
                    "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers("/api/setup", "/api/login", "/api/logout")
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable());
        return http.build();
    }
}
