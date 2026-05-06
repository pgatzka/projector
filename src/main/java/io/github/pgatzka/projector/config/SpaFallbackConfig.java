package io.github.pgatzka.projector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Forwards unmatched non-API routes to /index.html so the React Router SPA
 * can handle client-side routing. /api/** and /actuator/** keep their normal
 * 404 behavior because they're handled by controllers / actuator before this
 * fallback fires.
 */
@Configuration
public class SpaFallbackConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Single-segment paths (e.g. /projects, /settings).
        // Excludes static resources, API prefixes, and index.html itself (served by ResourceHttpRequestHandler).
        registry.addViewController("/{path:^(?!api|actuator|assets|static|v3|swagger-ui|favicon\\.ico|index\\.html).*$}")
                .setViewName("forward:/index.html");
        // Two-or-more-segment paths (e.g. /projects/abc, /issues/abc/edit).
        // Note: Spring MVC's /** matches an empty suffix, so this pattern also covers single-segment
        // paths not already handled above. Exclusion list must therefore be identical to the
        // single-segment pattern — including index.html and favicon.ico — so static resources
        // are never re-forwarded into an infinite loop.
        registry.addViewController("/{path:^(?!api|actuator|assets|static|v3|swagger-ui|favicon\\.ico|index\\.html).*$}/**")
                .setViewName("forward:/index.html");
    }
}
