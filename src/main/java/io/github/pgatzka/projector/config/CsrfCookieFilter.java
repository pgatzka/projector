package io.github.pgatzka.projector.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forces the CSRF token attribute to be resolved on every request so that the
 * XSRF-TOKEN cookie is written via CookieCsrfTokenRepository. Without this,
 * Spring Security 6+ defers token generation to the first reader, which means
 * GETs and CSRF-exempt endpoints (login, setup, logout) don't trigger the
 * cookie write — leaving the SPA without a token to send back on PATCH/DELETE.
 *
 * Standard Spring Security 6+/7 SPA pattern — see Spring Security migration
 * docs for "Defer Loading CsrfToken".
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            // Touch the token to force CookieCsrfTokenRepository to write the XSRF-TOKEN cookie.
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
